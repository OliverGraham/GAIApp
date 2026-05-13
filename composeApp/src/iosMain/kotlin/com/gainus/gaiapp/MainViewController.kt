package com.gainus.gaiapp

import androidx.compose.ui.window.ComposeUIViewController
import com.gainus.gaiapp.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}
