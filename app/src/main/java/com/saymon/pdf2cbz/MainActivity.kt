package com.saymon.pdf2cbz

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saymon.pdf2cbz.ui.ConvertViewModel
import com.saymon.pdf2cbz.ui.MainScreen
import com.saymon.pdf2cbz.ui.SettingsScreen
import com.saymon.pdf2cbz.ui.theme.Pdf2CbzTheme

class MainActivity : ComponentActivity() {
    private lateinit var vm: ConvertViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15 + targetSdk 35+: приложение рисуется под системные
        // панели, отступы и цвета иконок панелей разруливаем сами
        enableEdgeToEdge()
        vm = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ConvertViewModel::class.java]
        handleShare(intent)
        setContent { Root() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShare(intent)
    }

    /** Забирает PDF из «Поделиться» (один или несколько). */
    private fun handleShare(intent: Intent) {
        val uris = when (intent.action) {
            Intent.ACTION_SEND ->
                (intent.parcelable<Uri>(Intent.EXTRA_STREAM))?.let { listOf(it) }
            Intent.ACTION_SEND_MULTIPLE ->
                intent.parcelableList<Uri>(Intent.EXTRA_STREAM)
            else -> null
        }
        if (!uris.isNullOrEmpty()) vm.addUris(uris)
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? =
        if (android.os.Build.VERSION.SDK_INT >= 33) getParcelableExtra(key, T::class.java)
        else @Suppress("DEPRECATION") getParcelableExtra(key)

    private inline fun <reified T : Parcelable> Intent.parcelableList(key: String): List<T>? =
        if (android.os.Build.VERSION.SDK_INT >= 33) getParcelableArrayListExtra(key, T::class.java)
        else @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
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
        val window = (view.context as android.app.Activity).window
        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    Pdf2CbzTheme(darkTheme = dark) {
        // Системный жест «назад» в настройках — на главный экран, а не из приложения
        BackHandler(enabled = vm.showSettings) { vm.showSettings = false }
        if (vm.showSettings) SettingsScreen(vm) { vm.showSettings = false }
        else MainScreen(vm)
    }
}
