package com.saymon.pdf2cbz.ui

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.saymon.pdf2cbz.core.convert.JobCancelled
import com.saymon.pdf2cbz.core.convert.convertPdf
import com.saymon.pdf2cbz.core.convert.etaText
import com.saymon.pdf2cbz.core.convert.pageCount
import com.saymon.pdf2cbz.ConvertControl
import com.saymon.pdf2cbz.ConvertService
import com.saymon.pdf2cbz.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InFile(val uri: Uri, val name: String, val pages: Int)

class ConvertViewModel(app: Application) : AndroidViewModel(app) {
    private val settings = SettingsStore(app)
    val files = mutableStateListOf<InFile>()
    val doneUris = mutableStateListOf<String>()      // готовые за этот запуск
    val failed = mutableStateMapOf<String, String>() // uri -> ошибка
    var quality by mutableIntStateOf(90)
    var dpi by mutableIntStateOf(150)
    var theme by mutableIntStateOf(settings.theme)
    var showSettings by mutableStateOf(false)
    var outputLabel by mutableStateOf("Загрузки")
    var running by mutableStateOf(false)
    var status by mutableStateOf("Добавьте PDF кнопкой выше.")
    var curFile by mutableStateOf("")
    var fileDone by mutableIntStateOf(0)
    var fileTotal by mutableIntStateOf(0)
    var filesDone by mutableIntStateOf(0)
    var pagesAllDone by mutableIntStateOf(0)
    var pagesAllTotal by mutableIntStateOf(0)
    private val fileTimes = mutableListOf<Long>()

    fun chooseTheme(v: Int) {
        theme = v
        settings.theme = v
    }

    init {
        refreshOutputLabel()
    }

    fun refreshOutputLabel() {
        val uri = settings.outputDir?.let { Uri.parse(it) }
        outputLabel = if (uri != null) {
            try {
                androidx.documentfile.provider.DocumentFile
                    .fromTreeUri(getApplication(), uri)?.name ?: "Выбранная папка"
            } catch (_: Exception) {
                "Выбранная папка"
            }
        } else "Загрузки"
    }

    fun setOutputDir(uri: Uri) {
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } catch (_: Exception) {
        }
        settings.outputDir = uri.toString()
        refreshOutputLabel()
        if (!running) status = "Папка для CBZ: $outputLabel."
    }

    fun clearOutputDir() {
        settings.outputDir = null
        refreshOutputLabel()
        if (!running) status = "Папка для CBZ: Загрузки."
    }

    fun addUris(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = getApplication<Application>().contentResolver
            for (u in uris) {
                if (files.any { it.uri == u }) continue
                try {
                    res.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {
                }
                val pages = try { pageCount(res, u) } catch (_: Exception) { 0 }
                if (pages <= 0) continue
                var name = "doc.pdf"
                res.query(u, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex("_display_name")
                    if (c.moveToFirst() && idx >= 0) name = c.getString(idx) ?: name
                }
                files.add(InFile(u, name, pages))
            }
            if (!running) status = "Готово к конвертации: файлов ${files.size}."
        }
    }

    fun removeFile(f: InFile) {
        if (!running) files.remove(f)
    }

    fun clearAll() {
        if (!running) {
            files.clear()
            status = "Добавьте PDF кнопкой выше."
        }
    }

    fun cancel() {
        ConvertControl.cancelled.set(true)
    }

    fun start() {
        if (running || files.isEmpty()) return
        running = true
        ConvertControl.cancelled.set(false)
        filesDone = 0
        pagesAllDone = 0
        fileTimes.clear()
        doneUris.clear()
        failed.clear()
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = getApplication<Application>()
            val res = ctx.contentResolver
            ConvertService.cmd(ctx, ConvertService.ACTION_START)
            // Снимок очереди: список под нами может меняться только когда не бежим
            val queue = files.toList()
            val totalFiles = queue.size
            pagesAllTotal = queue.sumOf { it.pages }
            var pagesBefore = 0
            for ((fi, f) in queue.withIndex()) {
                if (ConvertControl.cancelled.get()) break
                curFile = f.name
                fileDone = 0
                fileTotal = f.pages
                val t0 = System.currentTimeMillis()
                try {
                    val stem = f.name.removeSuffix(".pdf").removeSuffix(".PDF")
                    val treeUri = settings.outputDir?.let { Uri.parse(it) }
                    if (treeUri != null) {
                        // Выбранная папка (SAF): создаём файл в ней
                        val tree = androidx.documentfile.provider.DocumentFile
                            .fromTreeUri(ctx, treeUri)
                            ?: throw IllegalStateException("Папка недоступна")
                        val doc = tree.createFile(
                            "application/vnd.comicbook+zip", "$stem.cbz")
                            ?: throw IllegalStateException("Не создался файл")
                        res.openOutputStream(doc.uri)?.use { out ->
                            convertPdf(res, f.uri, out, dpi, quality, ConvertControl.cancelled) { d, t ->
                                fileDone = d
                                fileTotal = t
                                pagesAllDone = pagesBefore + d
                            }
                        } ?: throw IllegalStateException("Не открылся выходной файл")
                    } else {
                        // Загрузки через MediaStore
                        val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, "$stem.cbz")
                        // MIME комиксов: с application/zip система дописывала
                        // лишний .zip -> test.cbz.zip
                        put(MediaStore.Downloads.MIME_TYPE,
                            "application/vnd.comicbook+zip")
                        if (Build.VERSION.SDK_INT >= 29) {
                            put(MediaStore.Downloads.RELATIVE_PATH,
                                Environment.DIRECTORY_DOWNLOADS)
                        }
                    }
                    val outUri = res.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: throw IllegalStateException("Нет доступа к Загрузкам")
                    res.openOutputStream(outUri)?.use { out ->
                        convertPdf(res, f.uri, out, dpi, quality, ConvertControl.cancelled) { d, t ->
                            fileDone = d
                            fileTotal = t
                            pagesAllDone = pagesBefore + d
                            // Шторку дёргаем не чаще, чем раз в 5 страниц
                            if (pagesAllDone % 5 == 0 || d == t) {
                                ConvertService.cmd(ctx, ConvertService.ACTION_UPDATE,
                                    pagesAllDone, pagesAllTotal, f.name)
                            }
                        }
                    } ?: throw IllegalStateException("Не открылся выходной файл")
                    } // else: Загрузки через MediaStore
                    fileTimes.add(System.currentTimeMillis() - t0)
                    filesDone = fi + 1
                    pagesBefore += f.pages
                    pagesAllDone = pagesBefore
                    doneUris.add(f.uri.toString())
                } catch (e: JobCancelled) {
                    status = "Остановлено на «${f.name}»."
                    break
                } catch (e: Exception) {
                    failed[f.uri.toString()] = e.message ?: "ошибка"
                    status = "«${f.name}»: не вышло: ${e.message} (остальные продолжу)"
                }
            }
            withContext(Dispatchers.Main) {
                if (!ConvertControl.cancelled.get() && failed.isEmpty() && filesDone == totalFiles) {
                    status = "Готово: файлов $totalFiles в папке «$outputLabel»."
                    files.clear()
                    doneUris.clear()
                } else if (!ConvertControl.cancelled.get()) {
                    status = "Готово частично: ok ${doneUris.size} из $totalFiles, " +
                        "ошибок ${failed.size}. " +
                        etaText(fileTimes, filesDone, totalFiles)
                }
            }
            ConvertService.cmd(ctx, ConvertService.ACTION_STOP)
            running = false
        }
    }
}
