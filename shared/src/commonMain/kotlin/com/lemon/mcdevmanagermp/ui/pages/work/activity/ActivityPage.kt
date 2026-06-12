package com.lemon.mcdevmanagermp.ui.pages.work.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.ActivityDetailPage
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.ActivityCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.ActivityExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.layout.ActivityMediumLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.ActivityParticipatePage
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

@Composable
fun ActivityPage(
    onBack: () -> Unit
) {
    val viewModel = remember { ActivityViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    // 内部子页面导航状态
    var selectedActivity by remember { mutableStateOf<ReviewActivityItemVO?>(null) }
    // 参与页面导航状态
    var participateTarget by remember { mutableStateOf<ReviewActivityItemVO?>(null) }

    val onParticipateSuccess: () -> Unit = {
        // 仅刷新活动列表数据，不清除导航状态（参与页面自行刷新模组列表）
        viewModel.dispatch(ActivityAction.RefreshData)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActivityEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(ActivityAction.LoadData)
    }

    // 所有宽度模式统一：参与页面全屏显示
    if (participateTarget != null) {
        ActivityParticipatePage(
            activity = participateTarget!!,
            onBack = { participateTarget = null },
            showTopBar = true,
            onSuccess = onParticipateSuccess
        )
        return
    }

    // 详情页面全屏显示
    if (selectedActivity != null) {
        ActivityDetailPage(
            activity = selectedActivity!!,
            onBack = { selectedActivity = null },
            onNavigateToParticipate = { participateTarget = it }
        )
        return
    }

    // 列表页面：根据宽度选择不同布局
    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        when (widthSizeClass) {
            WindowWidthSizeClass.Expanded -> ActivityExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                onItemClick = { selectedActivity = it }
            )

            WindowWidthSizeClass.Medium -> {
                ActivityMediumLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onBack = onBack,
                    onItemClick = { selectedActivity = it }
                )
            }

            else -> {
                ActivityCompactLayout(
                    state = state,
                    onAction = viewModel::dispatch,
                    onBack = onBack,
                    onItemClick = { selectedActivity = it }
                )
            }
        }
    }
}
