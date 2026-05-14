package com.gainus.gaiapp

import androidx.compose.ui.window.ComposeUIViewController
import com.gainus.gaiapp.di.initKoin
import com.gainus.gaiapp.presentation.ui.App

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}
