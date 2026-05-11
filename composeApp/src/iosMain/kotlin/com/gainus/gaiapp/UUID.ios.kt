package com.gainus.gaiapp

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()
