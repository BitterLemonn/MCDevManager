package com.lemon.mcdevmanagermp.ui.pages.work.activity.participate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityReviewItemVO
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun ActivityParticipatePage(
    activity: ActivityReviewItemVO,
    onBack: () -> Unit,
    showTopBar: Boolean = true,
    onSuccess: () -> Unit = {}
) {
    val viewModel = remember { ActivityParticipateViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(activity) {
        viewModel.dispatch(ActivityParticipateAction.LoadData(activity))
    }

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is ActivityParticipateEffect.ShowToast -> showToast(effect.message)

            is ActivityParticipateEffect.ParticipateSuccess -> {
                // 参与成功后刷新可参与模组列表
                state.activity?.let { viewModel.dispatch(ActivityParticipateAction.LoadData(it)) }
                onSuccess()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> ActivityParticipateCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                showTopBar = showTopBar
            )

            WindowWidthSizeClass.Medium -> ActivityParticipateMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                showTopBar = showTopBar
            )

            else -> ActivityParticipateExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                showTopBar = showTopBar
            )
        }
    }
}
