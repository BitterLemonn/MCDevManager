package com.lemon.mcdevmanagermp.ui.pages.community.feedback.layout

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
import com.lemon.mcdevmanagermp.ui.pages.community.components.EmptyDetailPlaceholder
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackAction
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackDetailPanel
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackFilterBar
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackListContent
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackState
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackTopBar
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_feedback

// ============================================================
// Expanded Layout (Master-Detail)
// ============================================================

@Composable
internal fun ExpandedFeedbackLayout(
    state: FeedbackState,
    onAction: (FeedbackAction) -> Unit,
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
        FeedbackTopBar(onBack = onBack, onRefresh = { onAction(FeedbackAction.Refresh) })

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navBarBottom)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().width(400.dp)
            ) {
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

            HorizontalDivider(
                modifier = Modifier.width(1.dp).fillMaxHeight(),
                color = colors.outlineVariant,
                thickness = 1.dp
            )

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val selectedId = state.selectedFeedback?.id
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
                    label = "feedback_detail_transition"
                ) { id ->
                    val feedback = state.feedbackList.find { it.id == id }
                    if (feedback != null) {
                        FeedbackDetailPanel(
                            feedback = feedback,
                            replyText = state.replyText,
                            isReplying = state.isReplying,
                            onReplyTextChange = { onAction(FeedbackAction.UpdateReplyText(it)) },
                            onSubmitReply = { onAction(FeedbackAction.SubmitReply(feedback.id)) },
                            onBack = { onAction(FeedbackAction.SelectFeedback(null)) },
                            navBarBottom = 0.dp
                        )
                    } else {
                        EmptyDetailPlaceholder(
                            icon = Res.drawable.ic_feedback,
                            hint = "选择一条反馈查看详情"
                        )
                    }
                }
            }
        }
    }
}
