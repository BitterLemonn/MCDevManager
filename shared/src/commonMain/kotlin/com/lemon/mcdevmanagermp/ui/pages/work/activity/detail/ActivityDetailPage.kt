package com.lemon.mcdevmanagermp.ui.pages.work.activity.detail

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

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = when {
            maxWidth < 600.dp -> WindowWidthSizeClass.Compact
            maxWidth < 840.dp -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }

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
