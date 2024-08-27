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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.lemon.mcdevmanager.data.common.FEEDBACK_PAGE
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.data.netease.feedback.FeedbackBean
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.HeaderHeight
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.CommentCard
import com.lemon.mcdevmanager.ui.widget.FABPositionWidget
import com.lemon.mcdevmanager.ui.widget.FeedbackCard
import com.lemon.mcdevmanager.ui.widget.FlowTabWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
import com.lemon.mcdevmanager.ui.widget.SNACK_SUCCESS
import com.lemon.mcdevmanager.ui.widget.SearchBarWidget
import com.lemon.mcdevmanager.utils.getNavigationBarHeight
import com.lemon.mcdevmanager.viewModel.CommentPageAction
import com.lemon.mcdevmanager.viewModel.CommentPageEvent
import com.lemon.mcdevmanager.viewModel.CommentPageViewModel
import com.lemon.mcdevmanager.viewModel.FeedbackAction
import com.lemon.mcdevmanager.viewModel.FeedbackEvent
import com.lemon.mcdevmanager.viewModel.FeedbackViewModel
import com.lt.compose_views.other.VerticalSpace
import com.lt.compose_views.refresh_layout.RefreshLayoutState
import com.lt.compose_views.refresh_layout.VerticalRefreshableLayout
import com.lt.compose_views.zoom.ImageViewer
import com.zj.mvi.core.observeEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommentPage(
    showToast: (String, String) -> Unit = { _, _ -> },
    navController: NavController,
    viewModel: CommentPageViewModel = viewModel()
) {
    val states by viewModel.viewStates.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val lazyState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyState.firstVisibleItemIndex } }

    var isShowDetail by remember { mutableStateOf(false) }

    val replyRequester = remember { FocusRequester() }
    var isFocusReply by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(CommentPageAction.LoadComment)
        viewModel.viewEvents.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is CommentPageEvent.ShowToast -> {
                    showToast(event.message, event.tag)
                }

                is CommentPageEvent.NeedReLogin -> navController.navigate(LOGIN_PAGE) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    val topRefreshState = remember {
        RefreshLayoutState {
            viewModel.dispatch(CommentPageAction.RefreshComment)
        }
    }
    val bottomRefreshState = remember {
        RefreshLayoutState {
            viewModel.dispatch(CommentPageAction.LoadComment)
        }
    }

    // 玩家反馈列表
    Column(Modifier.fillMaxSize()) {
        HeaderWidget(title = "玩家评论", leftAction = {
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
                    colorFilter = ColorFilter.tint(TextWhite)
                )
            }
        })
        SearchBarWidget(
            searchStr = states.key ?: "",
            onSearchStrChange = { viewModel.dispatch(CommentPageAction.UpdateKey(it.ifBlank { null })) },
            onSearch = { viewModel.dispatch(CommentPageAction.RefreshComment) },
            isWithFilter = true,
            isUseFilter = states.isUseFilter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "回复类型",
                        fontSize = 14.sp,
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                    )
                    FlowRow(Modifier.fillMaxWidth()) {
                        FlowTabWidget(text = "全部类型", isSelected = states.state == null) {
                            viewModel.dispatch(CommentPageAction.UpdateState(null))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "未回复", isSelected = states.state == 0) {
                            viewModel.dispatch(CommentPageAction.UpdateState(if (it) null else 0))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "已回复", isSelected = states.state == 1) {
                            viewModel.dispatch(CommentPageAction.UpdateState(if (it) null else 1))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                    }
                    VerticalSpace(dp = 8.dp)
                    Text(
                        text = "评论类型",
                        fontSize = 14.sp,
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    FlowRow(Modifier.fillMaxWidth()) {
                        FlowTabWidget(text = "默认类型", isSelected = states.tag.contains(0)) {
                            viewModel.dispatch(CommentPageAction.UpdateTag(0, !it))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "问题求助", isSelected = states.tag.contains(10)) {
                            viewModel.dispatch(CommentPageAction.UpdateTag(10, !it))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "模组评测", isSelected = states.tag.contains(20)) {
                            viewModel.dispatch(CommentPageAction.UpdateTag(20, !it))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "玩法攻略", isSelected = states.tag.contains(30)) {
                            viewModel.dispatch(CommentPageAction.UpdateTag(30, !it))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "建议反馈", isSelected = states.tag.contains(40)) {
                            viewModel.dispatch(CommentPageAction.UpdateTag(40, !it))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "其他讨论", isSelected = states.tag.contains(50)) {
                            viewModel.dispatch(CommentPageAction.UpdateTag(50, !it))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                    }
                    VerticalSpace(dp = 8.dp)
                    Text(
                        text = "评分",
                        fontSize = 14.sp,
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    FlowRow(Modifier.fillMaxWidth()) {
                        FlowTabWidget(text = "全部评分", isSelected = states.starFilter == 0) {
                            viewModel.dispatch(CommentPageAction.UpdateStarFilter(0))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "1星", isSelected = states.starFilter == 1) {
                            viewModel.dispatch(CommentPageAction.UpdateStarFilter(if (it) 0 else 1))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "2星", isSelected = states.starFilter == 2) {
                            viewModel.dispatch(CommentPageAction.UpdateStarFilter(if (it) 0 else 2))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "3星", isSelected = states.starFilter == 3) {
                            viewModel.dispatch(CommentPageAction.UpdateStarFilter(if (it) 0 else 3))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "4星", isSelected = states.starFilter == 4) {
                            viewModel.dispatch(CommentPageAction.UpdateStarFilter(if (it) 0 else 4))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                        FlowTabWidget(text = "5星", isSelected = states.starFilter == 5) {
                            viewModel.dispatch(CommentPageAction.UpdateStarFilter(if (it) 0 else 5))
                            viewModel.dispatch(CommentPageAction.RefreshComment)
                        }
                    }
                }
            }
        }
        VerticalRefreshableLayout(
            modifier = Modifier.weight(1f),
            topRefreshLayoutState = topRefreshState,
            bottomRefreshLayoutState = bottomRefreshState,
            bottomIsLoadFinish = states.isLoadOver,
            topUserEnable = false
        ) {
            LazyColumn(
                modifier = Modifier.animateContentSize(), state = lazyState
            ) {
                if (states.isLoading && states.commentList.isEmpty()) {
                    item {
                        AppLoadingWidget(showBackground = false)
                    }
                } else itemsIndexed(states.commentList) { index, item ->
                    CommentCard(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple()
                        ) {
//                            detailItem = item
                            isShowDetail = true
                            if (isFocusReply) keyboard?.show()
                        },
                        resName = item.resName,
                        iid = item.iid,
                        tag = item.commentTag,
                        comment = item.userComment,
                        nickname = item.nickname,
                        stars = item.stars.toInt(),
                        publishTime = item.publishTime
                    )
                    if (index == states.commentList.size - 5) {
                        viewModel.dispatch(CommentPageAction.LoadComment)
                    }
                }
            }
        }
    }

    // 悬浮按钮
    AnimatedVisibility(visible = firstVisibleItemIndex > 2, enter = fadeIn(), exit = fadeOut()) {
        FABPositionWidget {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch { lazyState.animateScrollToItem(0) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(2.dp, AppTheme.colors.primaryColor, CircleShape),
                shape = CircleShape,
                containerColor = AppTheme.colors.primaryColor,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back_to_top),
                    contentDescription = "up",
                    modifier = Modifier.size(36.dp),
                    colorFilter = ColorFilter.tint(TextWhite)
                )
            }
        }
    }

//    // 详情页
//    AnimatedVisibility(
//        visible = isShowDetail,
//        enter = expandVertically() + fadeIn(),
//        exit = shrinkVertically() + fadeOut()
//    ) {
//        LaunchedEffect(key1 = Unit) {
//            viewModel.dispatch(FeedbackAction.UpdateReplyId(detailItem.id))
//        }
//
//        Box(modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black.copy(alpha = 0.8f))
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() }, indication = null
//            ) {
//                resetDetail()
//            }
//            .verticalScroll(rememberScrollState())
//            .imePadding()) {
//            FeedbackCard(modName = detailItem.resName,
//                modUid = detailItem.iid,
//                createTime = detailItem.createTime,
//                type = detailItem.type,
//                nickname = detailItem.commitNickname,
//                content = detailItem.content,
//                picList = detailItem.picList,
//                reply = detailItem.reply,
//                onClickImg = { url ->
//                    bigImageUrl = url
//                },
//                isShowReply = true,
//                extraContent = {
//                    Row(
//                        Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp)
//                            .border(1.dp, AppTheme.colors.primaryColor, RoundedCornerShape(8.dp))
//                    ) {
//                        Box(modifier = Modifier
//                            .weight(1f)
//                            .align(Alignment.CenterVertically)
//                            .padding(start = 8.dp)
//                            .clickable(
//                                interactionSource = remember { MutableInteractionSource() },
//                                indication = null
//                            ) { replyRequester.requestFocus() }) {
//                            Text(
//                                text = states.replyContent,
//                                fontSize = 16.sp,
//                                color = AppTheme.colors.textColor,
//                                modifier = Modifier
//                                    .padding(8.dp)
//                                    .align(Alignment.CenterStart)
//                            )
//                        }
//                        Box(modifier = Modifier
//                            .width(80.dp)
//                            .padding(8.dp)
//                            .align(Alignment.CenterVertically)
//                            .clip(RoundedCornerShape(8.dp))
//                            .clickable(
//                                interactionSource = remember { MutableInteractionSource() },
//                                indication = rememberRipple()
//                            ) {
//                                viewModel.dispatch(FeedbackAction.ReplyFeedback)
//                                keyboard?.hide()
//                            }) {
//                            Text(
//                                text = "回复",
//                                fontSize = 16.sp,
//                                color = AppTheme.colors.primaryColor,
//                                modifier = Modifier
//                                    .padding(8.dp)
//                                    .align(Alignment.Center)
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                })
//        }
//
//    }

//    // 回复框
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .offset(y = getNavigationBarHeight(context).dp)
//            .imePadding()
//            .alpha(if (isShowDetail && isFocusReply) 1f else 0f)
//    ) {
//        OutlinedTextField(enabled = isShowDetail,
//            value = states.replyContent,
//            onValueChange = {
//                viewModel.dispatch(FeedbackAction.UpdateReplyContent(it))
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.BottomCenter)
//                .focusRequester(replyRequester)
//                .onFocusChanged {
//                    isFocusReply = it.isFocused
//                },
//            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
//            keyboardActions = KeyboardActions(onSend = {
//                viewModel.dispatch(FeedbackAction.ReplyFeedback)
//                keyboard?.hide()
//                isFocusReply = false
//            }),
//            shape = RoundedCornerShape(8.dp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = AppTheme.colors.primaryColor,
//                unfocusedBorderColor = AppTheme.colors.card,
//                focusedContainerColor = AppTheme.colors.card,
//                unfocusedContainerColor = AppTheme.colors.card,
//                focusedTextColor = AppTheme.colors.textColor,
//                unfocusedLabelColor = AppTheme.colors.textColor,
//            ),
//            trailingIcon = {
//                Box(modifier = Modifier
//                    .width(80.dp)
//                    .padding(end = 8.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .clickable(
//                        interactionSource = remember { MutableInteractionSource() },
//                        indication = rememberRipple()
//                    ) {
//                        viewModel.dispatch(FeedbackAction.ReplyFeedback)
//                        keyboard?.hide()
//                        isFocusReply = false
//                    }) {
//                    Text(
//                        text = "回复",
//                        fontSize = 16.sp,
//                        color = AppTheme.colors.primaryColor,
//                        modifier = Modifier
//                            .padding(8.dp)
//                            .align(Alignment.Center)
//                    )
//                }
//            })
//    }

    // loading
//    AnimatedVisibility(
//        visible = states.isLoadingReply, enter = fadeIn(), exit = fadeOut()
//    ) {
//        AppLoadingWidget()
//    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FeedbackPagePreview() {
    CommentPage(
        showToast = { _, _ -> }, navController = rememberNavController()
    )
}