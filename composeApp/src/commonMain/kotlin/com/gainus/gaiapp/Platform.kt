package com.gai.gaiapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
