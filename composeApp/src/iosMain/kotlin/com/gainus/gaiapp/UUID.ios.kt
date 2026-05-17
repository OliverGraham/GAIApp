package com.gai.gaiapp.util

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()
