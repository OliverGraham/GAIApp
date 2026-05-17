package com.gai.gaiapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gai.gaiapp.di.initKoin
import com.gai.gaiapp.presentation.ui.App
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin { modules(module { single<Context> { this@MainActivity.applicationContext } }) }

        setContent { App() }
    }
}
