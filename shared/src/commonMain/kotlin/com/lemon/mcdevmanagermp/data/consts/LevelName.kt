package com.lemon.mcdevmanagermp.data.consts

fun getLevelName(level: Int): String = when (level) {
    1 -> "元气新星"
    2 -> "巧手工匠"
    3 -> "杰出精英"
    4 -> "创造大师"
    5 -> "传奇宗师"
    else -> "元气新星"
}
