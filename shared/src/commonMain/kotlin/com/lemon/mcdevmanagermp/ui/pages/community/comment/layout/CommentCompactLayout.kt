package com.lemon.mcdevmanagermp.ui.pages.community.comment.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentAction
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentDetailPanel
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentFilterBar
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentListContent
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentState
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentTopBar
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
internal fun CompactCommentLayout(
    state: CommentState,
    allTags: List<String>,
    onAction: (CommentAction) -> Unit,
    onBack: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    val showDetail = state.selectedComment != null

    AnimatedContent(
        targetState = showDetail,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)))
            } else {
                (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)))
            }
        },
        label = "comment_compact"
    ) { showingDetail ->
        if (showingDetail && state.selectedComment != null) {
            CommentDetailPanel(
                comment = state.selectedComment,
                replyText = state.replyText,
                isReplying = state.isReplying,
                onReplyTextChange = { onAction(CommentAction.UpdateReplyText(it)) },
                onSubmitReply = { onAction(CommentAction.SubmitReply(state.selectedComment.id)) },
                onBack = { onAction(CommentAction.SelectComment(null)) },
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalAppColors.current.background)
            ) {
                CommentTopBar(onBack = onBack, onRefresh = { onAction(CommentAction.Refresh) })

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
        }
    }
}
