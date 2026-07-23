package com.lemon.mcdevmanagermp.ui.pages.community

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.ui.iconpack.Comment
import com.lemon.mcdevmanagermp.ui.iconpack.Feedback
import com.lemon.mcdevmanagermp.ui.iconpack.IconPack
import com.lemon.mcdevmanagermp.ui.pages.community.comment.CommentPage
import com.lemon.mcdevmanagermp.ui.pages.community.feedback.FeedbackPage
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

private enum class CommunitySubPage { List, Feedback, Comment }

@Composable
fun CommunityContent() {
    var currentSubPage by remember { mutableStateOf(CommunitySubPage.List) }

    BackHandler(enabled = currentSubPage != CommunitySubPage.List) {
        currentSubPage = CommunitySubPage.List
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    AnimatedContent(
        targetState = currentSubPage,
        transitionSpec = {
            if (targetState == CommunitySubPage.List) {
                (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)))
            } else {
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)))
            }
        },
        label = "community_subpage"
    ) { targetPage ->
        when (targetPage) {
            CommunitySubPage.List -> CommunityListPage(
                statusBarTop = statusBarTop,
                onNavigateToFeedback = { currentSubPage = CommunitySubPage.Feedback },
                onNavigateToComment = { currentSubPage = CommunitySubPage.Comment }
            )

            CommunitySubPage.Feedback -> FeedbackPage(
                onBack = { currentSubPage = CommunitySubPage.List }
            )

            CommunitySubPage.Comment -> CommentPage(
                onBack = { currentSubPage = CommunitySubPage.List }
            )
        }
    }
}

@Composable
private fun CommunityListPage(
    statusBarTop: androidx.compose.ui.unit.Dp,
    onNavigateToFeedback: () -> Unit,
    onNavigateToComment: () -> Unit
) {
    val colors = LocalAppColors.current
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = statusBarTop)
            .padding(bottom = navBarBottom)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "互动管理",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textColor
        )

        Spacer(Modifier.height(4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh),
            shape = RoundedCornerShape(16.dp),

        ) {
            CommunityItem(
                icon = IconPack.Feedback,
                title = "玩家反馈",
                subtitle = "查看和回复玩家的反馈意见",
                onClick = onNavigateToFeedback
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colors.outlineVariant,
                thickness = 0.5.dp
            )

            CommunityItem(
                icon = IconPack.Comment,
                title = "组件评论",
                subtitle = "查看和回复玩家的组件评论",
                onClick = onNavigateToComment
            )
        }
    }
}

@Composable
private fun CommunityItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bgColor = if (isHovered) colors.primary.copy(alpha = 0.06f) else androidx.compose.ui.graphics.Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.textColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        Text(
            text = "›",
            fontSize = 20.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
