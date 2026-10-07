package com.saymon.pdf2cbz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.saymon.pdf2cbz.ui.MainScreen
import com.saymon.pdf2cbz.ui.theme.Pdf2CbzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pdf2CbzTheme { MainScreen() }
        }
    }
}
