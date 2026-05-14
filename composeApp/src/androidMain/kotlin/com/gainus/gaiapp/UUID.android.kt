package com.gainus.gaiapp.util

import java.util.UUID

actual fun randomUUID(): String = UUID.randomUUID().toString()
