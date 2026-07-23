package com.lemon.mcdevmanagermp.ui.pages.main

import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_analyze
import mcdevmanagermpr.shared.generated.resources.ic_comment
import mcdevmanagermpr.shared.generated.resources.ic_dashboard
import mcdevmanagermpr.shared.generated.resources.ic_setting
import mcdevmanagermpr.shared.generated.resources.ic_work
import org.jetbrains.compose.resources.DrawableResource

enum class MainTab(val label: String, val icon: DrawableResource) {
    Home("首页", Res.drawable.ic_dashboard),
    Work("作品管理", Res.drawable.ic_work),
    Analyze("数据分析", Res.drawable.ic_analyze),
    Community("互动管理", Res.drawable.ic_comment),
    Settings("设置", Res.drawable.ic_setting)
}
