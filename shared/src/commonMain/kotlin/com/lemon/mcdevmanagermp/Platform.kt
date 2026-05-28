package com.lemon.mcdevmanagermp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform