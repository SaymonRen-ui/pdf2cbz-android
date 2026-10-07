package com.saymon.pdf2cbz.core.convert

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class JobCancelled : Exception("Отменено")

data class ConvertResult(val pages: Int)

/** Имена как на ПК: ширина по числу страниц — 001.jpg … 240.jpg. */
fun pageName(index: Int, total: Int): String {
    val width = total.toString().length
    return (index + 1).toString().padStart(width, '0') + ".jpg"
}

/** Сколько страниц в PDF, не конвертируя. */
fun pageCount(resolver: ContentResolver, uri: Uri): Int {
    val pfd = resolver.openFileDescriptor(uri, "r") ?: return 0
    pfd.use {
        PdfRenderer(it).use { return it.pageCount }
    }
}

/**
 * Рендерит PDF в JPEG и пишет .cbz (ZIP без сжатия, как на ПК) в поток.
 * onPage(done, total) — прогресс. Бросает JobCancelled при флаге.
 */
fun convertPdf(
    resolver: ContentResolver,
    uri: Uri,
    out: OutputStream,
    dpi: Int,
    quality: Int,
    cancelled: AtomicBoolean,
    onPage: (done: Int, total: Int) -> Unit,
): ConvertResult {
    val pfd = resolver.openFileDescriptor(uri, "r")
        ?: throw IllegalArgumentException("Не открылся PDF")
    pfd.use {
        PdfRenderer(it).use { renderer ->
            val total = renderer.pageCount
            if (total == 0) throw IllegalArgumentException("В PDF нет страниц")
            ZipOutputStream(out.buffered()).use { zip ->
                for (i in 0 until total) {
                    if (cancelled.get()) throw JobCancelled()
                    renderer.openPage(i).use { page ->
                        var scale = dpi / 72f
                        // Защита от гигантских страниц: ужимаем dpi, но не ниже 72
                        var w = (page.width * scale).toInt().coerceAtLeast(1)
                        var h = (page.height * scale).toInt().coerceAtLeast(1)
                        var curDpi = dpi
                        while (w.toLong() * h > 64L * 1024 * 1024 && curDpi > 72) {
                            curDpi -= 12
                            scale = curDpi / 72f
                            w = (page.width * scale).toInt().coerceAtLeast(1)
                            h = (page.height * scale).toInt().coerceAtLeast(1)
                        }
                        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        Canvas(bmp).drawColor(Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        val bos = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, quality, bos)
                        bmp.recycle()
                        val data = bos.toByteArray()
                        val entry = ZipEntry(pageName(i, total))
                        entry.method = ZipEntry.STORED
                        entry.size = data.size.toLong()
                        val crc = CRC32()
                        crc.update(data)
                        entry.crc = crc.value
                        zip.putNextEntry(entry)
                        zip.write(data)
                        zip.closeEntry()
                    }
                    onPage(i + 1, total)
                }
            }
            return ConvertResult(total)
        }
    }
}

/** ETA как на ПК: среднее время файла × осталось, только после 1 готового. */
fun etaText(fileTimesMs: List<Long>, doneFiles: Int, totalFiles: Int): String {
    if (doneFiles <= 0 || doneFiles >= totalFiles || fileTimesMs.isEmpty()) return ""
    val avg = fileTimesMs.average()
    val leftMs = (avg * (totalFiles - doneFiles)).toLong()
    val s = leftMs / 1000
    return if (s < 3600) "осталось ~${s / 60} мин ${s % 60} с"
    else "осталось ~${s / 3600} ч ${(s % 3600) / 60} мин"
}
