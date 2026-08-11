package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.RichDetailForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.BasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.ChannelImageForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PcBasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PcResourceManageForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PeResourceManageForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PeUpdateSummaryForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PriceInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.ShelfSettingsForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.VideoUploadForm
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
internal fun WorkDetailTopBarActions(
    isSubmitting: Boolean,
    onAction: (WorkDetailAction) -> Unit
) {
    val contentColor = LocalContentColor.current
    val buttonColors = ButtonDefaults.textButtonColors(
        contentColor = contentColor,
        disabledContentColor = contentColor.copy(alpha = 0.38f)
    )

    TextButton(
        onClick = { onAction(WorkDetailAction.Save) },
        enabled = !isSubmitting,
        colors = buttonColors
    ) {
        Text("保存")
    }
    TextButton(
        onClick = { onAction(WorkDetailAction.SaveAndReview) },
        enabled = !isSubmitting,
        colors = buttonColors
    ) {
        Text("提审", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun WorkDetailInfoSections(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    columns: Int,
    showMetaRow: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WorkDetailGroupTitle("作品信息")
        BasicInfoForm(
            state = state,
            onAction = onAction,
            modifier = Modifier.fillMaxWidth(),
            columns = columns,
            showMetaRow = showMetaRow
        )
        if (state.syncPc) {
            PcBasicInfoForm(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
        PriceInfoForm(
            state = state,
            onAction = onAction,
            modifier = Modifier.fillMaxWidth()
        )
        RichDetailForm(
            title = "PE 详情信息",
            html = state.detail?.info ?: "",
            echoKey = state.detail?.itemId,
            onHtmlChange = { onAction(WorkDetailAction.UpdatePeDetail(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        PeUpdateSummaryForm(
            state = state,
            onAction = onAction,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.syncPc) {
            RichDetailForm(
                title = "PC 详细信息",
                html = state.detail?.syncItemInfo?.info ?: "",
                echoKey = state.detail?.itemId,
                onHtmlChange = { onAction(WorkDetailAction.UpdatePcDetail(it)) },
                syncFromPeHtml = { state.peDetail },
                showPreviewButton = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
        ShelfSettingsForm(
            title = "PE 上架设置",
            weakOffline = state.peWeakOffline,
            reason = state.peWeakOfflineReason,
            onToggleWeakOffline = { onAction(WorkDetailAction.TogglePeWeakOffline(it)) },
            onReasonChange = { onAction(WorkDetailAction.UpdatePeWeakOfflineReason(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        if (state.syncPc) {
            ShelfSettingsForm(
                title = "PC 上架设置",
                weakOffline = state.pcWeakOffline,
                reason = state.pcWeakOfflineReason,
                onToggleWeakOffline = { onAction(WorkDetailAction.TogglePcWeakOffline(it)) },
                onReasonChange = { onAction(WorkDetailAction.UpdatePcWeakOfflineReason(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun WorkDetailMediaSections(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WorkDetailGroupTitle("资源与媒体")
        PeResourceManageForm(
            state = state,
            onAction = onAction,
            modifier = Modifier.fillMaxWidth()
        )
        ChannelImageForm(
            title = "编辑 PE 图片",
            slots = state.peImageSlots,
            onSelect = { channel, file, metadata ->
                onAction(WorkDetailAction.SelectPeChannelImage(channel, file, metadata))
            },
            onRemove = { onAction(WorkDetailAction.RemovePeChannelImage(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        VideoUploadForm(
            state = state,
            onAction = onAction,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.syncPc) {
            PcResourceManageForm(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth()
            )
            ChannelImageForm(
                title = "编辑 PC 图片",
                slots = state.pcImageSlots,
                onSelect = { channel, file, metadata ->
                    onAction(WorkDetailAction.SelectPcChannelImage(channel, file, metadata))
                },
                onRemove = { onAction(WorkDetailAction.RemovePcChannelImage(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WorkDetailGroupTitle(title: String) {
    val colors = LocalAppColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = colors.textColor
    )
}
