package com.lemon.mcdevmanagermp.ui.pages.community.comment.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentAction
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentDetailPanel
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentFilterBar
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentListContent
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentState
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentTopBar
import com.lemon.mcdevmanagermp.ui.pages.community.components.EmptyDetailPlaceholder
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_comment_line

@Composable
internal fun ExpandedCommentLayout(
    state: CommentState,
    allTags: List<String>,
    onAction: (CommentAction) -> Unit,
    onBack: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = statusBarTop)
    ) {
        CommentTopBar(onBack = onBack, onRefresh = { onAction(CommentAction.Refresh) })

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navBarBottom)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().width(400.dp)
            ) {
                CommentFilterBar(
                    state = state,
                    allTags = allTags,
                    onAction = onAction
                )

                CommentListContent(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.width(1.dp).fillMaxHeight(),
                color = colors.outlineVariant,
                thickness = 1.dp
            )

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val selectedId = state.selectedComment?.id
                AnimatedContent(
                    targetState = selectedId,
                    transitionSpec = {
                        if (targetState == null) {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        } else if (initialState == null) {
                            (slideInHorizontally(tween(250)) { it / 3 } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally(tween(250)) { -it / 3 } + fadeOut(tween(250)))
                        } else {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        }
                    },
                    label = "comment_detail_transition"
                ) { id ->
                    val comment = state.commentList.find { it.id == id }
                    if (comment != null) {
                        CommentDetailPanel(
                            comment = comment,
                            replyText = state.replyText,
                            isReplying = state.isReplying,
                            onReplyTextChange = { onAction(CommentAction.UpdateReplyText(it)) },
                            onSubmitReply = { onAction(CommentAction.SubmitReply(comment.id)) },
                            onBack = { onAction(CommentAction.SelectComment(null)) },
                            statusBarTop = 0.dp,
                            navBarBottom = 0.dp
                        )
                    } else {
                        EmptyDetailPlaceholder(
                            icon = Res.drawable.ic_comment_line,
                            hint = "选择一条评论查看详情"
                        )
                    }
                }
            }
        }
    }
}
