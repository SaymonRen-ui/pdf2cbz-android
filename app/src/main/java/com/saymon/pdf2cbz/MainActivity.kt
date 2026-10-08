package com.saymon.pdf2cbz

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saymon.pdf2cbz.ui.ConvertViewModel
import com.saymon.pdf2cbz.ui.MainScreen
import com.saymon.pdf2cbz.ui.SettingsScreen
import com.saymon.pdf2cbz.ui.theme.Pdf2CbzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15 + targetSdk 35+: приложение рисуется под системные
        // панели, отступы и цвета иконок панелей разруливаем сами
        enableEdgeToEdge()
        setContent { Root() }
    }
}

@Composable
private fun Root() {
    val ctx = LocalContext.current
    val vm: ConvertViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(ctx.applicationContext as Application)
    )
    val dark = when (vm.theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    // Иконки системных панелей под тему: на светлой — тёмные, иначе их не видно
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    Pdf2CbzTheme(darkTheme = dark) {
        if (vm.showSettings) SettingsScreen(vm) { vm.showSettings = false }
        else MainScreen(vm)
    }
}
