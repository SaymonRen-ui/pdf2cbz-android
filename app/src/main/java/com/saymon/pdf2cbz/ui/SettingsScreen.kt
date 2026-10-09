package com.saymon.pdf2cbz.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Настройки: тема и папка для готовых .cbz. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: ConvertViewModel, onBack: () -> Unit) {
    val pickDir = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> if (uri != null) vm.setOutputDir(uri) }

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
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)
            .verticalScroll(rememberScrollState()),
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
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Папка для готовых .cbz",
                        style = MaterialTheme.typography.titleMedium)
                    Text(vm.outputLabel,
                        color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pickDir.launch(null) },
                            enabled = !vm.running) { Text("Выбрать") }
                        TextButton(onClick = { vm.clearOutputDir() },
                            enabled = !vm.running) { Text("По умолчанию") }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            AboutCard()
        }
    }
}

/** О приложении: версия, ссылки, лицензии. */
@Composable
private fun AboutCard() {
    val context = LocalContext.current
    val version = remember {
        try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val info = if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(context.packageName,
                    PackageManager.PackageInfoFlags.of(0))
            } else {
                pm.getPackageInfo(context.packageName, 0)
            }
            info.versionName ?: ""
        } catch (_: Exception) {
            ""
        }
    }
    fun openUrl(url: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        } catch (_: Exception) {
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("О приложении",
                style = MaterialTheme.typography.titleMedium)
            Text(if (version.isNotEmpty()) "PDF2CBZ $version · конвертер PDF в CBZ для читалок"
                else "PDF2CBZ · конвертер PDF в CBZ для читалок",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { openUrl(
                "https://github.com/SaymonRen-ui/pdf2cbz-android/blob/main/PRIVACY.md") },
                modifier = Modifier.fillMaxWidth()) {
                Text("Политика конфиденциальности")
            }
            OutlinedButton(onClick = { openUrl(
                "https://github.com/SaymonRen-ui/pdf2cbz-android") },
                modifier = Modifier.fillMaxWidth()) {
                Text("Исходный код на GitHub")
            }
            OutlinedButton(onClick = {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_SENDTO,
                            android.net.Uri.parse("mailto:app.project@bk.ru")).apply {
                            putExtra(Intent.EXTRA_SUBJECT, "PDF2CBZ")
                        })
                } catch (_: Exception) {
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Написать в поддержку")
            }
            Text("AndroidX, Jetpack Compose — Apache 2.0. " +
                "Yandex Mobile Ads SDK — проприетарный SDK Яндекса для показа рекламы.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
