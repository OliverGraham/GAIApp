package com.gai.gaiapp.di

import com.gai.gaiapp.data.local.database.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module { single { getDatabaseBuilder() } }
