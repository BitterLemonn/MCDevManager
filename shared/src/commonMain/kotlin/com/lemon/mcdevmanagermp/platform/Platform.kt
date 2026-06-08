package com.lemon.mcdevmanagermp.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun supportsDynamicColor(): Boolean
