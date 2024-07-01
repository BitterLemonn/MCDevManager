package com.lemon.mcdevmanager.ui.page

import android.text.TextUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.netease.feedback.FeedbackBean
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.HeaderHeight
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.FeedbackCard
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SearchBarWidget
import com.lemon.mcdevmanager.viewModel.FeedbackAction
import com.lemon.mcdevmanager.viewModel.FeedbackEvent
import com.lemon.mcdevmanager.viewModel.FeedbackViewModel
import com.lt.compose_views.refresh_layout.RefreshLayoutState
import com.lt.compose_views.refresh_layout.VerticalRefreshableLayout
import com.lt.compose_views.zoom.ImageViewer
import com.zj.mvi.core.observeEvent
import kotlinx.coroutines.delay

@Composable
fun FeedbackPage(
    showToast: (String, String) -> Unit = { _, _ -> },
    navController: NavController,
    viewModel: FeedbackViewModel = viewModel()
) {
    val states by viewModel.viewStates.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current

    var bigImageUrl by remember { mutableStateOf("") }
    var detailItem by remember { mutableStateOf(FeedbackBean()) }
    val imageLoader = remember { ImageLoader.Builder(context).crossfade(true).build() }

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(FeedbackAction.LoadFeedback)
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is FeedbackEvent.ShowToast -> showToast(event.msg, SNACK_ERROR)
                is FeedbackEvent.RouteToPath -> navController.navigate(event.path)
                is FeedbackEvent.ReplySuccess -> {
                    viewModel.dispatch(FeedbackAction.RefreshFeedback)
                    detailItem = FeedbackBean()
                }
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
            Box(modifier = Modifier
                .clip(shape = CircleShape)
                .padding(4.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple()
                ) {
                    keyboard?.hide()
                    navController.navigateUp()
                }) {
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
            bottomRefreshLayoutState = bottomRefreshState,
            bottomIsLoadFinish = !states.isLoadingList,
            topUserEnable = false
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                if (states.isLoadingList && states.feedbackList.isEmpty()) {
                    item {
                        AppLoadingWidget()
                    }
                } else itemsIndexed(states.feedbackList) { index, item ->
                    FeedbackCard(id = item.id,
                        modName = item.resName,
                        modUid = item.iid,
                        createTime = item.createTime,
                        type = item.type,
                        content = item.content,
                        picList = item.picList,
                        reply = item.reply,
                        onClickImg = { url ->
                            bigImageUrl = url
                        },
                        onClick = {
                            detailItem = item
                        })

                    if (index == states.feedbackList.size - 5) {
                        viewModel.dispatch(FeedbackAction.LoadFeedback)
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = detailItem.id != "0",
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        var waitTime by remember { mutableIntStateOf(2) }
        LaunchedEffect(key1 = Unit) {
            while (waitTime > 0) {
                waitTime -= 1
                delay(300)
            }
            viewModel.dispatch(FeedbackAction.UpdateReplyId(detailItem.id))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { detailItem = FeedbackBean() }
                .imePadding()
        ) {
            Column(Modifier.fillMaxWidth()) {
                FeedbackCard(
                    id = detailItem.id,
                    modName = detailItem.resName,
                    modUid = detailItem.iid,
                    createTime = detailItem.createTime,
                    type = detailItem.type,
                    content = "${detailItem.commitNickname}: \n${detailItem.content}",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .heightIn(max = 600.dp),
                    picList = detailItem.picList,
                    reply = detailItem.reply,
                    onClickImg = {
                        bigImageUrl = it
                    },
                    isExpanded = true
                )
                if (waitTime <= 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card)
                    ) {
                        if (!TextUtils.isEmpty(detailItem.reply)) {
                            Text(
                                text = detailItem.reply!!,
                                color = AppTheme.colors.textColor,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        OutlinedTextField(
                            value = states.replyContent,
                            onValueChange = {
                                viewModel.dispatch(FeedbackAction.UpdateReplyContent(it))
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = rememberRipple()
                                        ) { viewModel.dispatch(FeedbackAction.ReplyFeedback) }
                                ) {
                                    Text(
                                        text = "回复",
                                        color = AppTheme.colors.primaryColor,
                                        fontSize = 16.sp,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "回复",
                                    color = AppTheme.colors.hintColor,
                                    fontSize = 16.sp
                                )
                            },
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.dispatch(FeedbackAction.ReplyFeedback)
                                keyboard?.hide()
                            }),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                }
            }

        }

    }

    AnimatedVisibility(
        visible = !TextUtils.isEmpty(bigImageUrl),
        enter = fadeIn(animationSpec = tween(300)) + expandIn(
            animationSpec = tween(800), expandFrom = Alignment.Center
        ),
        exit = fadeOut(animationSpec = tween(300)) + shrinkOut(
            animationSpec = tween(800), shrinkTowards = Alignment.Center
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
        ) {
            ImageViewer(
                painter = rememberAsyncImagePainter(
                    model = bigImageUrl, imageLoader = imageLoader
                ), modifier = Modifier.align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .padding(top = HeaderHeight, start = 8.dp, end = 8.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "close",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(36.dp)
                        .align(Alignment.CenterStart)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple()
                        ) {
                            bigImageUrl = ""
                        },
                )
            }
        }
    }

    AnimatedVisibility(
        visible = states.isLoadingReply,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AppLoadingWidget()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FeedbackPagePreview() {
    FeedbackPage(
        showToast = { _, _ -> }, navController = rememberNavController()
    )
}