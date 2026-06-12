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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.ActivityParticipateMediumLayout
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

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

    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(activity) {
        viewModel.dispatch(ActivityParticipateAction.LoadData(activity))
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ActivityParticipateEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                is ActivityParticipateEffect.ParticipateSuccess -> {
                    // 参与成功后刷新可参与模组列表
                    state.activity?.let { viewModel.dispatch(ActivityParticipateAction.LoadData(it)) }
                    onSuccess()
                }
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
