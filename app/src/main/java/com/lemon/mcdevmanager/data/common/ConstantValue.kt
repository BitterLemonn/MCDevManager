package com.lemon.mcdevmanager.data.common

import kotlinx.serialization.json.Json

// navigation 地址
const val SPLASH_PAGE = "SPLASH_PAGE"
const val LOGIN_PAGE = "LOGIN_PAGE"


const val pkid = "kBSLIYY"
const val pd = "x19_developer"

const val SM4Key = "BC60B8B9E4FFEFFA219E5AD77F11F9E2"
const val RSAKey =
    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5gsH+AA4XWONB5TDcUd+xCz7ejOFHZKlcZDx+pF1i7Gsvi1vjyJoQhRtRSn950x498VUkx7rUxg1/ScBVfrRxQOZ8xFBye3pjAzfb22+RCuYApSVpJ3OO3KsEuKExftz9oFBv3ejxPlYc5yq7YiBO8XlTnQN0Sa4R4qhPO3I2MQIDAQAB"

val JSONConverter = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}