package com.lemon.mcdevmanagermp.ui.pages.main

import org.jetbrains.compose.resources.DrawableResource
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_analyze
import mcdevmanagermpr.shared.generated.resources.ic_comment_line
import mcdevmanagermpr.shared.generated.resources.ic_dashboard
import mcdevmanagermpr.shared.generated.resources.ic_feedback
import mcdevmanagermpr.shared.generated.resources.ic_setting

enum class MainTab(val label: String, val icon: DrawableResource) {
    Home("首页", Res.drawable.ic_dashboard),
    Analyze("数据分析", Res.drawable.ic_analyze),
    Feedback("玩家反馈", Res.drawable.ic_feedback),
    Comment("组件评论", Res.drawable.ic_comment_line),
    Settings("设置", Res.drawable.ic_setting)
}
