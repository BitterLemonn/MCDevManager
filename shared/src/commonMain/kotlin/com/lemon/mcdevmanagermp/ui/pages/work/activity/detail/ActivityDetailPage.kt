package com.lemon.mcdevmanagermp.ui.pages.work.activity.detail

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
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.layout.ActivityDetailCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.layout.ActivityDetailExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.detail.layout.ActivityDetailMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun ActivityDetailPage(
    activity: ReviewActivityItemVO,
    onBack: () -> Unit,
    showTopBar: Boolean = true,
    onNavigateToParticipate: (ReviewActivityItemVO) -> Unit = {}
) {
    val viewModel = remember { ActivityDetailViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(activity) {
        viewModel.dispatch(ActivityDetailAction.LoadData(activity))
    }

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is ActivityDetailEffect.ShowToast -> showToast(effect.message)

            is ActivityDetailEffect.NavigateToParticipate -> {
                onNavigateToParticipate(effect.activity)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current

        val onParticipate: (ReviewActivityItemVO) -> Unit = { target ->
            onNavigateToParticipate(target)
        }

        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> ActivityDetailCompactLayout(
                state = state,
                onBack = onBack,
                showTopBar = showTopBar,
                onParticipate = onParticipate
            )

            WindowWidthSizeClass.Medium -> ActivityDetailMediumLayout(
                state = state,
                onBack = onBack,
                showTopBar = showTopBar,
                onParticipate = onParticipate
            )

            else -> ActivityDetailExpandedLayout(
                state = state,
                onBack = onBack,
                showTopBar = showTopBar,
                onParticipate = onParticipate
            )
        }
    }
}
