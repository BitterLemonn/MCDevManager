package com.lemon.mcdevmanagermp.ui.pages.analyze

/**
 * 数据分析子页面定义
 * 用于在 Analyze Tab 内部切换子页面，而非 NavHost 导航
 */
sealed interface AnalyzeSubPage {
    data object RealtimeProfit : AnalyzeSubPage
    data class ModAnalysis(val iid: String = "", val platform: String = "pe") : AnalyzeSubPage
}
