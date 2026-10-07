package com.saymon.pdf2cbz

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saymon.pdf2cbz.ui.ConvertViewModel
import com.saymon.pdf2cbz.ui.MainScreen
import com.saymon.pdf2cbz.ui.SettingsScreen
import com.saymon.pdf2cbz.ui.theme.Pdf2CbzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Root() }
    }
}

@Composable
private fun Root() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val vm: ConvertViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(ctx.applicationContext as Application)
    )
    val dark = when (vm.theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    Pdf2CbzTheme(darkTheme = dark) {
        if (vm.showSettings) SettingsScreen(vm) { vm.showSettings = false }
        else MainScreen(vm)
    }
}
