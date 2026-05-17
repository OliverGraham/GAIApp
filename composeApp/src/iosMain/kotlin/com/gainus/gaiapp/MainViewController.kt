package com.gai.gaiapp

import androidx.compose.ui.window.ComposeUIViewController
import com.gai.gaiapp.di.initKoin
import com.gai.gaiapp.presentation.ui.App

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}
