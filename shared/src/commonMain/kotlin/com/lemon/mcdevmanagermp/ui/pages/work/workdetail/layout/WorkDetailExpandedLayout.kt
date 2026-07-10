package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.components.RichDetailForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.BasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.ChannelImageForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.MetaInfoBar
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PcBasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PcResourceManageForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PeResourceManageForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PeUpdateSummaryForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PlaceholderModule
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PlaceholderSection
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PriceInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.ShelfSettingsForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.VideoUploadForm
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 桌面布局（>840dp）：顶部作品信息条 + 双栏并排。
 * 左栏 = 信息与配置（基本信息 / PC 基本 / 定价 / PE 详情 / PE 纪要 / PC 详情 / PE 上架 / PC 上架），
 * 右栏 = 资源与媒体（PE 资源 / PE 图片 / 轮播 / 视频 / PC 资源 / PC 图片）。
 * 两栏内部均遵循统一模块顺序，限宽 1200dp 居中。
 */
@Composable
internal fun WorkDetailExpandedLayout(
    state: WorkDetailState,
    onAction: (WorkDetailAction) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val scrollState = rememberScrollState()
    val scrollAlpha = remember {
        derivedStateOf { (scrollState.value / 100f).coerceIn(0f, 1f) }
    }
    val contentColor = lerp(colors.textColor, colors.scheme.onPrimary, scrollAlpha.value)

    Column(modifier = Modifier.fillMaxSize()) {
        CollapsingTopBar(
            title = "作品详情",
            collapseFraction = scrollAlpha.value,
            onBack = onBack,
            actions = {
                TextButton(onClick = { onAction(WorkDetailAction.Submit) }) {
                    Text("更新", color = contentColor)
                }
            }
        )

        when {
            state.isLoading && state.detail == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = colors.primary,
                    strokeWidth = 3.dp
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 限宽居中：超宽屏下表单居中、两侧留白，字段不被拉散
                Column(
                    modifier = Modifier.widthIn(max = 1200.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 顶部作品信息条（只读元数据横排展示）
                    MetaInfoBar(state = state)
                    // 双栏：左信息配置 / 右资源媒体，同屏可见，充分利用宽屏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 左栏：信息与配置（模块顺序 1-8）
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. 基本信息
                            BasicInfoForm(
                                state = state,
                                onAction = onAction,
                                modifier = Modifier.fillMaxWidth(),
                                columns = 2,
                                showMetaRow = false
                            )
                            // 2. PC 基本信息（同步生成 PC 时）
                            if (state.syncPc) {
                                PcBasicInfoForm(
                                    state = state,
                                    onAction = onAction,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            // 3. 资源定价
                            PriceInfoForm(
                                state = state,
                                onAction = onAction,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // 4. PE 详情信息
                            RichDetailForm(
                                title = "PE 详情信息",
                                html = state.detail?.info ?: "",
                                echoKey = state.detail?.itemId,
                                onHtmlChange = { onAction(WorkDetailAction.UpdatePeDetail(it)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            // 5. PE 更新纪要
                            PeUpdateSummaryForm(
                                state = state,
                                onAction = onAction,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // 6. PC 详情信息（同步生成 PC 时）
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
                            // 7. PE 上架设置
                            ShelfSettingsForm(
                                title = "PE 上架设置",
                                weakOffline = state.peWeakOffline,
                                reason = state.peWeakOfflineReason,
                                onToggleWeakOffline = {
                                    onAction(
                                        WorkDetailAction.TogglePeWeakOffline(
                                            it
                                        )
                                    )
                                },
                                onReasonChange = {
                                    onAction(
                                        WorkDetailAction.UpdatePeWeakOfflineReason(
                                            it
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            // 8. PC 上架设置（同步生成 PC 时）
                            if (state.syncPc) {
                                ShelfSettingsForm(
                                    title = "PC 上架设置",
                                    weakOffline = state.pcWeakOffline,
                                    reason = state.pcWeakOfflineReason,
                                    onToggleWeakOffline = {
                                        onAction(
                                            WorkDetailAction.TogglePcWeakOffline(
                                                it
                                            )
                                        )
                                    },
                                    onReasonChange = {
                                        onAction(
                                            WorkDetailAction.UpdatePcWeakOfflineReason(
                                                it
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        // 右栏：资源与媒体（模块顺序 9-14）
                        Column(
                            modifier = Modifier.weight(0.82f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 9. 上传 PE 资源管理
                            PeResourceManageForm(
                                state = state,
                                onAction = onAction,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // 10. 编辑 PE 图片
                            ChannelImageForm(
                                title = "编辑 PE 图片",
                                slots = state.peImageSlots,
                                onSelect = { c, f, m ->
                                    onAction(
                                        WorkDetailAction.SelectPeChannelImage(
                                            c,
                                            f,
                                            m
                                        )
                                    )
                                },
                                onRemove = { onAction(WorkDetailAction.RemovePeChannelImage(it)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            // 11. PE 资源中心首页轮播推广图
                            PlaceholderSection(
                                module = PlaceholderModule.PE_CAROUSEL,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // 12. 上传视频
                            VideoUploadForm(
                                state = state,
                                onAction = onAction,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // 13/14. 上传 PC 模组信息 / 编辑 PC 图片（同步生成 PC 时）
                            if (state.syncPc) {
                                PcResourceManageForm(
                                    state = state,
                                    onAction = onAction,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                ChannelImageForm(
                                    title = "编辑 PC 图片",
                                    slots = state.pcImageSlots,
                                    onSelect = { c, f, m ->
                                        onAction(
                                            WorkDetailAction.SelectPcChannelImage(
                                                c,
                                                f,
                                                m
                                            )
                                        )
                                    },
                                    onRemove = { onAction(WorkDetailAction.RemovePcChannelImage(it)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
