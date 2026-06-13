package com.lemon.mcdevmanagermp.ui.pages.work

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanagermp.platform.BackHandler
import com.lemon.mcdevmanagermp.ui.pages.work.activity.ActivityPage
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManagePage
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_mod
import mcdevmanagermpr.shared.generated.resources.ic_sale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private enum class WorkSubPage { List, Activity, WorkManage }

@Composable
fun WorkContent() {
    var currentSubPage by remember { mutableStateOf(WorkSubPage.List) }

    BackHandler(enabled = currentSubPage != WorkSubPage.List) {
        currentSubPage = WorkSubPage.List
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    AnimatedContent(
        targetState = currentSubPage,
        transitionSpec = {
            if (targetState == WorkSubPage.List) {
                (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)))
            } else {
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)))
            }
        },
        label = "work_subpage"
    ) { targetPage ->
        when (targetPage) {
            WorkSubPage.List -> WorkListPage(
                statusBarTop = statusBarTop,
                onNavigateToActivity = { currentSubPage = WorkSubPage.Activity },
                onNavigateToWorkManage = { currentSubPage = WorkSubPage.WorkManage }
            )

            WorkSubPage.Activity -> ActivityPage(
                onBack = { currentSubPage = WorkSubPage.List }
            )

            WorkSubPage.WorkManage -> WorkManagePage(
                onBack = { currentSubPage = WorkSubPage.List }
            )
        }
    }
}

@Composable
private fun WorkListPage(
    statusBarTop: androidx.compose.ui.unit.Dp,
    onNavigateToActivity: () -> Unit,
    onNavigateToWorkManage: () -> Unit
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
            text = "作品管理",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textColor
        )

        Spacer(Modifier.height(4.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            WorkItem(
                icon = Res.drawable.ic_sale,
                title = "作品活动",
                subtitle = "查看和参与平台作品活动",
                onClick = onNavigateToActivity
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            WorkItem(
                icon = Res.drawable.ic_mod,
                title = "上架管理",
                subtitle = "管理作品上架与审核状态",
                onClick = onNavigateToWorkManage
            )
        }
    }
}

@Composable
private fun WorkItem(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bgColor = if (isHovered) colors.primary.copy(alpha = 0.06f) else Color.Transparent

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
                painter = painterResource(icon),
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
