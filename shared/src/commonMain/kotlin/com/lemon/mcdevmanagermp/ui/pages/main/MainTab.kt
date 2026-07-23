package com.lemon.mcdevmanagermp.ui.pages.main

import androidx.compose.ui.graphics.vector.ImageVector
import com.lemon.mcdevmanagermp.ui.iconpack.Analyze
import com.lemon.mcdevmanagermp.ui.iconpack.Comment
import com.lemon.mcdevmanagermp.ui.iconpack.Dashboard
import com.lemon.mcdevmanagermp.ui.iconpack.IconPack
import com.lemon.mcdevmanagermp.ui.iconpack.Setting
import com.lemon.mcdevmanagermp.ui.iconpack.Work

enum class MainTab(val label: String, val icon: ImageVector) {
    Home("首页", IconPack.Dashboard),
    Work("作品管理", IconPack.Work),
    Analyze("数据分析", IconPack.Analyze),
    Community("互动管理", IconPack.Comment),
    Settings("设置", IconPack.Setting)
}
