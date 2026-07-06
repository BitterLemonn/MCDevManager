package com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ActivityCandidatesItemVO
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.ActivityParticipateAction
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.ActivityParticipateState
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.component.ImageSelectorRow
import com.lemon.mcdevmanagermp.ui.pages.work.activity.participate.layout.component.VideoSelectorBox
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_add
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ActivityParticipateCompactLayout(
    state: ActivityParticipateState,
    onAction: (ActivityParticipateAction) -> Unit,
    onBack: () -> Unit,
    showTopBar: Boolean = true
) {
    val colors = LocalAppColors.current
    val activity = state.activity ?: return
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (showTopBar) {
            CollapsingTopBar(
                title = "参与活动",
                collapseFraction = 0f,
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 活动名称摘要
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainerHigh)
                    .padding(14.dp)
            ) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor,
                    maxLines = 1
                )
            }

            // Module 选择 Tabs
            if (activity.modules.size > 1) {
                ModuleTabRow(
                    modules = activity.modules.map { it.moduleName to it.moduleId },
                    selectedModuleId = state.selectedModuleId,
                    onModuleSelected = { onAction(ActivityParticipateAction.SelectModule(it)) },
                    colors = colors
                )
            } else if (activity.modules.size == 1) {
                // 单个 module 直接显示名称
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainerHigh)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "赛道: ${activity.modules.first().moduleName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textColor
                    )
                }
            }

            // 候选作品列表
            Text(
                text = "选择作品",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = colors.primary,
                        strokeWidth = 2.dp
                    )
                }
            } else if (state.candidates.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainerHigh)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂无可参与的作品",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            } else {
                state.candidates.forEach { candidate ->
                    CandidateItem(
                        candidate = candidate,
                        isSelected = state.selectedCandidateId == candidate.itemId,
                        onSelect = { onAction(ActivityParticipateAction.SelectCandidate(candidate.itemId)) },
                        colors = colors
                    )
                }
            }

            // 申请简介
            Text(
                text = "申请简介",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textColor
            )

            OutlinedTextField(
                value = state.applyIntro,
                onValueChange = { onAction(ActivityParticipateAction.UpdateApplyIntro(it)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = {
                    Text(
                        text = "请输入申请简介（可选）",
                        color = colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outlineVariant,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                )
            )

            // 图片上传
            ImageSelectorRow(
                selectedImages = state.selectedImages,
                onAddImages = { onAction(ActivityParticipateAction.AddImages(it)) },
                onRemoveImage = { onAction(ActivityParticipateAction.RemoveImage(it)) },
                enabled = !state.isUploading && !state.isSubmitting,
                colors = colors
            )

            // 视频上传
            VideoSelectorBox(
                selectedVideo = state.selectedVideo,
                onAddVideo = { onAction(ActivityParticipateAction.AddVideo(it)) },
                onRemoveVideo = { onAction(ActivityParticipateAction.RemoveVideo) },
                onValidationError = { onAction(ActivityParticipateAction.ValidationError(it)) },
                enabled = !state.isUploading && !state.isSubmitting,
                colors = colors
            )

            // 上传进度
            if (state.isUploading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = colors.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = state.uploadProgress,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary
                    )
                }
            }

            // 提交按钮
            FilledTonalButton(
                onClick = { onAction(ActivityParticipateAction.Submit) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isSubmitting && !state.isUploading && state.selectedCandidateId != null
            ) {
                if (state.isSubmitting || state.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = colors.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = when {
                        state.isUploading -> "上传中..."
                        state.isSubmitting -> "提交中..."
                        else -> "提交参与"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModuleTabRow(
    modules: List<Pair<String, Int>>,
    selectedModuleId: Int?,
    onModuleSelected: (Int) -> Unit,
    colors: com.lemon.mcdevmanagermp.ui.theme.AppColors
) {
    val selectedIndex = modules.indexOfFirst { it.second == selectedModuleId }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        containerColor = colors.surfaceContainerHigh,
        contentColor = colors.textColor,
        indicator = { tabPositions ->
            if (tabPositions.isNotEmpty() && selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = colors.primary,
                    height = 3.dp
                )
            }
        }
    ) {
        modules.forEachIndexed { index, (name, id) ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onModuleSelected(id) },
                text = {
                    Text(
                        text = name,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == selectedIndex) colors.primary else colors.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
private fun CandidateItem(
    candidate: ActivityCandidatesItemVO,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colors: com.lemon.mcdevmanagermp.ui.theme.AppColors
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.08f) else colors.surfaceContainerHigh)
            .then(
                if (isSelected) Modifier.border(
                    1.dp,
                    colors.primary.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.primary,
                unselectedColor = colors.onSurfaceVariant
            )
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = candidate.itemName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textColor
            )
            Text(
                text = if (candidate.price > 0) "${candidate.price} ${candidate.priceTypeName}" else "免费",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}
