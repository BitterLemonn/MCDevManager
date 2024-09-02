package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.ui.fragPage.AnalysisFragPage
import com.lemon.mcdevmanager.ui.fragPage.BetaFunctionFragPage
import com.lemon.mcdevmanager.ui.fragPage.OverviewFragPage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.NavigationItem
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_INFO
import com.lemon.mcdevmanager.utils.pxToDp
import com.lemon.mcdevmanager.viewModel.AnalyzeAction
import com.lemon.mcdevmanager.viewModel.AnalyzeEvent
import com.lemon.mcdevmanager.viewModel.AnalyzeViewModel
import com.zj.mvi.core.observeEvent
import kotlinx.coroutines.launch

@Composable
fun AnalyzePage(
    navController: NavController,
    showToast: (String, String) -> Unit,
    viewModel: AnalyzeViewModel = viewModel()
) {
    val states by viewModel.viewStates.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fullHeight by remember { mutableIntStateOf(0) }

    var isShowDetail by remember { mutableStateOf(false) }

    val chartColor = AppTheme.colors.chartColors
    val animateNavHeight by animateFloatAsState(targetValue = if (!isShowDetail) 56f else 0f)
    val pageState = rememberPagerState { 3 }

    val nowPage by rememberUpdatedState(pageState.currentPage)

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(AnalyzeAction.UpdateChartColor(chartColor))
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is AnalyzeEvent.ShowToast -> showToast(
                    event.msg, if (event.isError) SNACK_ERROR else SNACK_INFO
                )

                is AnalyzeEvent.NeedReLogin -> navController.navigate(LOGIN_PAGE) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { fullHeight = pxToDp(context, it.size.height.toFloat()) }
    ) {
        Column {
            // 标题
            HeaderWidget(title = "数据分析", leftAction = {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .clickable(indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() }) { navController.navigateUp() }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "back"
                    )
                }
            })
            // 页面内容
            HorizontalPager(
                state = pageState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f)
            ) {
                when (it) {
                    0 -> AnalysisFragPage(
                        viewModel = viewModel,
                        showToast = showToast
                    )

                    1 -> OverviewFragPage(
                        viewModel = viewModel,
                        showToast = showToast
                    )

                    else -> BetaFunctionFragPage(
                        navController = navController,
                        showToast = showToast
                    )
                }
            }
            // 底部导航
            Column {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.dividerColor,
                    thickness = 0.5.dp
                )
                BottomNavigation(
                    backgroundColor = AppTheme.colors.card,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animateNavHeight.dp),
                    elevation = 0.dp
                ) {
                    Spacer(modifier = Modifier.weight(0.5f))
                    NavigationItem(
                        title = "分析",
                        icon = R.drawable.ic_analyze,
                        isSelected = nowPage == 0
                    ) {
                        coroutineScope.launch { pageState.animateScrollToPage(0) }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    NavigationItem(
                        title = "总览",
                        icon = R.drawable.ic_total,
                        isSelected = nowPage == 1
                    ) {
                        coroutineScope.launch { pageState.animateScrollToPage(1) }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    NavigationItem(
                        title = "Beta",
                        icon = R.drawable.ic_beta,
                        isSelected = nowPage == 2
                    ) {
                        coroutineScope.launch { pageState.animateScrollToPage(2) }
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }
        }

        // 加载中
        AnimatedVisibility(
            visible = states.isShowLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            AppLoadingWidget()
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun AnalyzePagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            AnalyzePage(navController = rememberNavController(), showToast = { _, _ -> })
        }
    }
}