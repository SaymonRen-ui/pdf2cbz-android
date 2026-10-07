package com.saymon.pdf2cbz.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Настройки: пока только тема. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: ConvertViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Тема")
            for ((v, label) in listOf(0 to "Системная", 1 to "Светлая", 2 to "Тёмная")) {
                Row {
                    RadioButton(selected = vm.theme == v,
                        onClick = { vm.chooseTheme(v) })
                    TextButton(onClick = { vm.chooseTheme(v) }) { Text(label) }
                }
            }
        }
    }
}
