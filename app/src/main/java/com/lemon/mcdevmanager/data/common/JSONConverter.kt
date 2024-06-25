package com.lemon.mcdevmanager.data.common

import kotlinx.serialization.json.Json

val JSONConverter = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}