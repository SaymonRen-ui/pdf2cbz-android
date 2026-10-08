package com.saymon.pdf2cbz.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: ConvertViewModel) {
    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> vm.addUris(uris) }
    // Пока идёт конвертация — не гасить экран
    val view = LocalView.current
    LaunchedEffect(vm.running) { view.keepScreenOn = vm.running }
    val totalPages = vm.files.sumOf { it.pages }

    Scaffold(
        // Контент внутри безопасной зоны: кнопки не уползут под навигацию
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("PDF2CBZ") },
                actions = {
                    IconButton(onClick = { vm.showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Сводка очереди
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Файлов: ${vm.files.size}",
                            style = MaterialTheme.typography.titleMedium)
                        Text("Страниц всего: $totalPages",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { pick.launch(arrayOf("application/pdf")) },
                            enabled = !vm.running) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.size(4.dp))
                            Text("PDF")
                        }
                        OutlinedButton(onClick = { vm.clearAll() },
                            enabled = !vm.running && vm.files.isNotEmpty()) {
                            Text("Очистить")
                        }
                    }
                }
            }
            if (vm.files.isEmpty() && !vm.running) {
                // Пустое состояние вместо дыры
                Column(Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Info, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Пока пусто",
                        style = MaterialTheme.typography.titleMedium)
                    Text("Нажмите «+ PDF» и выберите файлы.\nГотовые .cbz лягут в Загрузки.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(vm.files, key = { it.uri.toString() }) { f ->
                        val key = f.uri.toString()
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(f.name)
                                    val sub = when {
                                        vm.failed.containsKey(key) ->
                                            "✗ ${vm.failed[key]}"
                                        vm.doneUris.contains(key) -> "✓ готов"
                                        vm.running && vm.curFile == f.name ->
                                            "… ${vm.fileDone}/${vm.fileTotal}"
                                        else -> "стр. ${f.pages}"
                                    }
                                    Text(sub, color = when {
                                        vm.failed.containsKey(key) ->
                                            MaterialTheme.colorScheme.error
                                        vm.doneUris.contains(key) ->
                                            MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    })
                                }
                                if (!vm.running) {
                                    IconButton(onClick = { vm.removeFile(f) }) {
                                        Icon(Icons.Filled.Close,
                                            contentDescription = "Убрать")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Параметры — одной карточкой
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Качество JPEG: ${vm.quality}",
                        style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = vm.quality.toFloat(),
                        onValueChange = { vm.quality = it.toInt() },
                        valueRange = 1f..100f, enabled = !vm.running)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("DPI:", fontSize = 15.sp)
                        for (d in listOf(100, 150, 200, 300)) {
                            FilterChip(selected = vm.dpi == d,
                                onClick = { vm.dpi = d },
                                enabled = !vm.running, label = { Text("$d") })
                        }
                    }
                }
            }
            if (vm.running) {
                // Общий прогресс по всем страницам очереди
                LinearProgressIndicator(
                    progress = {
                        if (vm.pagesAllTotal > 0)
                            vm.pagesAllDone.toFloat() / vm.pagesAllTotal else 0f
                    },
                    modifier = Modifier.fillMaxWidth())
                Text("Всего: ${vm.pagesAllDone}/${vm.pagesAllTotal} стр. · " +
                    "файлов ${vm.filesDone}/${vm.files.size}")
                Text(vm.curFile)
                LinearProgressIndicator(
                    progress = { if (vm.fileTotal > 0) vm.fileDone.toFloat() / vm.fileTotal else 0f },
                    modifier = Modifier.fillMaxWidth())
                Text("${vm.fileDone}/${vm.fileTotal} стр. текущего файла")
                Button(onClick = { vm.cancel() }, modifier = Modifier.fillMaxWidth()) {
                    Text("✖ Отмена")
                }
            } else {
                Button(onClick = { vm.start() },
                    enabled = vm.files.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Конвертировать в CBZ (в Загрузки)")
                }
            }
            Text(vm.status)
        }
    }
}
