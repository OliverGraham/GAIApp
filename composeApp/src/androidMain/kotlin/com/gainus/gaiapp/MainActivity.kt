package com.gainus.gaiapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gainus.gaiapp.di.initKoin
import com.gainus.gaiapp.presentation.ui.App
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin { modules(module { single<Context> { this@MainActivity.applicationContext } }) }

        setContent { App() }
    }
}
