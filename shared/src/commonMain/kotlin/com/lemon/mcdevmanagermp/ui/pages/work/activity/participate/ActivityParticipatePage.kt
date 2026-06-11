package com.lemon.mcdevmanagermp.ui.pages.work.activity.participate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun ActivityParticipatePage(
    activity: ReviewActivityItemVO,
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

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActivityParticipateEffect.ShowToast -> {
                    // Toast 由上层处理
                }

                is ActivityParticipateEffect.ParticipateSuccess -> {
                    onSuccess()
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = when {
            maxWidth < 600.dp -> WindowWidthSizeClass.Compact
            maxWidth < 840.dp -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }

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
