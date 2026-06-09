package com.lemon.mcdevmanagermp.ui.pages.analyze

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.ui.pages.analyze.modAnalysis.ModAnalysisPage
import com.lemon.mcdevmanagermp.ui.pages.analyze.realtimeProfit.RealtimeProfitPage

/**
 * 数据分析 Tab 内容包装
 * 管理 Tab 内部的子页面导航：列表 ↔ 子页面
 */
@Composable
fun AnalyzeTabContent(
    onNavigateToSubPage: (Route) -> Unit
) {
    var currentSubPage: AnalyzeSubPage? by remember { mutableStateOf(null) }

    AnimatedContent(
        targetState = currentSubPage,
        transitionSpec = {
            if (targetState != null && initialState == null) {
                // 进入子页面：从右滑入
                (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it })
                    .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it })
            } else {
                // 返回列表：从左滑入
                (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it })
                    .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it })
            }
        },
        label = "analyze_subpage"
    ) { subPage ->
        when (subPage) {
            null -> AnalyzeContent(
                onNavigateToSubPage = { currentSubPage = it }
            )

            is AnalyzeSubPage.RealtimeProfit -> RealtimeProfitPage(
                onBack = { currentSubPage = null }
            )

            is AnalyzeSubPage.ModAnalysis -> ModAnalysisPage(
                initialIid = subPage.iid,
                initialPlatform = subPage.platform,
                onBack = { currentSubPage = null }
            )
        }
    }
}
