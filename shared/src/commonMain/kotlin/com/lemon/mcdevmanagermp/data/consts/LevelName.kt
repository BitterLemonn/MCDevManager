package com.lemon.mcdevmanagermp.data.consts

fun getLevelName(level: Int): String = when (level) {
    1 -> "元气新星"
    2 -> "巧手工匠"
    3 -> "杰出精英"
    4 -> "创造大师"
    5 -> "传奇宗师"
    else -> "元气新星"
}

/**
 * 贡献等级名映射（组件贡献 / 网络游戏 共用）
 * ponytail: 分级文案搬自旧项目 MainUserCard，若官方分级调整需同步
 */
fun getContributeClassName(classValue: Int): String = when (classValue) {
    1 -> "一览众山小"
    2 -> "起飞时刻"
    3 -> "奋斗老铁"
    4 -> "咸鱼潜水"
    5 -> "躺平的村民"
    else -> "躺平的村民"
}
