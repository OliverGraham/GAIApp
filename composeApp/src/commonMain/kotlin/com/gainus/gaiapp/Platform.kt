package com.gainus.gaiapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform