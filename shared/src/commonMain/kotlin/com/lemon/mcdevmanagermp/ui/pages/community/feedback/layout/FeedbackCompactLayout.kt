package com.lemon.mcdevmanagermp.ui.pages.community.feedback.layout

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
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackAction
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackDetailPanel
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackFilterBar
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackListContent
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackState
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackTopBar
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

// ============================================================
// Compact / Medium Layout
// ============================================================

@Composable
internal fun CompactFeedbackLayout(
    state: FeedbackState,
    onAction: (FeedbackAction) -> Unit,
    onBack: () -> Unit,
    statusBarTop: Dp,
    navBarBottom: Dp
) {
    val showDetail = state.selectedFeedback != null

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
        label = "feedback_compact"
    ) { showingDetail ->
        if (showingDetail && state.selectedFeedback != null) {
            FeedbackDetailPanel(
                feedback = state.selectedFeedback,
                replyText = state.replyText,
                isReplying = state.isReplying,
                onReplyTextChange = { onAction(FeedbackAction.UpdateReplyText(it)) },
                onSubmitReply = { onAction(FeedbackAction.SubmitReply(state.selectedFeedback.id)) },
                onBack = { onAction(FeedbackAction.SelectFeedback(null)) },
                navBarBottom = navBarBottom
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalAppColors.current.background)
            ) {
                FeedbackTopBar(onBack = onBack, onRefresh = { onAction(FeedbackAction.Refresh) })

                FeedbackFilterBar(
                    state = state,
                    onAction = onAction
                )

                FeedbackListContent(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
