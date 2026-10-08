package com.saymon.pdf2cbz.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Настройки: пока только тема. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: ConvertViewModel, onBack: () -> Unit) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Оформление",
                        style = MaterialTheme.typography.titleMedium)
                    Text("Как выглядит приложение",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    for ((v, label) in listOf(
                        0 to "Системная", 1 to "Светлая", 2 to "Тёмная")) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = vm.theme == v,
                                onClick = { vm.chooseTheme(v) })
                            TextButton(onClick = { vm.chooseTheme(v) }) {
                                Text(label)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("PDF2CBZ 1.0.0 · файлы сохраняются в Загрузки",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
