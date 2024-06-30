package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.widget.FeedbackCard
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SearchBarWidget
import com.lemon.mcdevmanager.viewModel.FeedbackAction
import com.lemon.mcdevmanager.viewModel.FeedbackEvent
import com.lemon.mcdevmanager.viewModel.FeedbackViewModel
import com.lt.compose_views.refresh_layout.RefreshLayoutState
import com.lt.compose_views.refresh_layout.VerticalRefreshableLayout
import com.zj.mvi.core.observeEvent

@Composable
fun FeedbackPage(
    showToast: (String, String) -> Unit = { _, _ -> },
    navController: NavController,
    viewModel: FeedbackViewModel = viewModel()
) {
    val states by viewModel.viewStates.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(FeedbackAction.LoadFeedback)
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is FeedbackEvent.ShowToast -> showToast(event.msg, SNACK_ERROR)
                is FeedbackEvent.RouteToPath -> navController.navigate(event.path)
            }
        }
    }

    val topRefreshState = remember {
        RefreshLayoutState {
//            viewModel.dispatch(FeedbackAction.LoadFeedback)
        }
    }
    val bottomRefreshState = remember {
        RefreshLayoutState {
            viewModel.dispatch(FeedbackAction.LoadFeedback)
        }
    }

    Column(Modifier.fillMaxSize()) {
        HeaderWidget(title = "玩家反馈", leftAction = {
            Box(
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .padding(4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) {
                        keyboard?.hide()
                        navController.navigateUp()
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .then(it),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(AppTheme.colors.textColor)
                )
            }
        })
        SearchBarWidget()
        VerticalRefreshableLayout(
            topRefreshLayoutState = topRefreshState,
            bottomRefreshLayoutState = bottomRefreshState
        ) {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(states.feedbackList) {
                    FeedbackCard(
                        id = it.id,
                        modName = it.resName,
                        modUid = it.iid,
                        createTime = it.createTime,
                        type = it.type,
                        content = it.content,
                        picList = it.picList,
                        reply = it.reply
                    ) {

                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FeedbackPagePreview() {
    FeedbackPage(
        showToast = { _, _ -> },
        navController = rememberNavController()
    )
}