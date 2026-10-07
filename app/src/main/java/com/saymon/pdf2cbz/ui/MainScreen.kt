package com.saymon.pdf2cbz.ui

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val ctx = LocalContext.current
    val vm: ConvertViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(ctx.applicationContext as Application)
    )
    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> vm.addUris(uris) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("PDF2CBZ") }) }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { pick.launch(arrayOf("application/pdf")) },
                    enabled = !vm.running) { Text("+ PDF") }
                OutlinedButton(onClick = { vm.clearAll() },
                    enabled = !vm.running && vm.files.isNotEmpty()) { Text("Очистить") }
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(vm.files, key = { it.uri.toString() }) { f ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(f.name)
                                Text("стр. ${f.pages}",
                                    color = androidx.compose.material3.MaterialTheme
                                        .colorScheme.onSurfaceVariant)
                            }
                            if (!vm.running) {
                                TextButton(onClick = { vm.removeFile(f) }) { Text("✕") }
                            }
                        }
                    }
                }
            }
            Text("Качество JPEG: ${vm.quality}")
            Slider(value = vm.quality.toFloat(), onValueChange = { vm.quality = it.toInt() },
                valueRange = 1f..100f, enabled = !vm.running)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DPI:")
                for (d in listOf(100, 150, 200, 300)) {
                    FilterChip(selected = vm.dpi == d, onClick = { vm.dpi = d },
                        enabled = !vm.running, label = { Text("$d") })
                }
            }
            if (vm.running) {
                Text(vm.curFile)
                LinearProgressIndicator(
                    progress = { if (vm.fileTotal > 0) vm.fileDone.toFloat() / vm.fileTotal else 0f },
                    modifier = Modifier.fillMaxWidth())
                Text("${vm.fileDone}/${vm.fileTotal} стр. · файлов ${vm.filesDone}/${vm.files.size}")
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
            Spacer(Modifier.height(4.dp))
        }
    }
}
