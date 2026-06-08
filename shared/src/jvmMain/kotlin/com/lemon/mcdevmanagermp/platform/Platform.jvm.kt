package com.lemon.mcdevmanagermp.platform

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun supportsDynamicColor(): Boolean = false
