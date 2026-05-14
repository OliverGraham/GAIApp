package com.gainus.gaiapp.di

import android.content.Context
import com.gainus.gaiapp.database.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module { single { getDatabaseBuilder(get<Context>()) } }
