package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.BasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.MetaInfoBar
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PcBasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PeDetailForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PriceInfoForm
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 平板布局（600-840dp）：顶部作品信息条（只读元数据横排仪表盘式）+ 双列网格编辑表单，
 * 短字段两两并排，长字段整行，容器铺满充分利用宽度。
 */
@Composable
internal fun WorkDetailMediumLayout(
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 顶部作品信息条（只读元数据横排展示，与 Expanded 视觉统一）
                MetaInfoBar(state = state)
                // 基本信息（元数据已由顶栏展示，关闭卡片内元数据组）
                BasicInfoForm(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    columns = 2,
                    showMetaRow = false
                )
                PeDetailForm(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth()
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
            }
        }
    }
}
