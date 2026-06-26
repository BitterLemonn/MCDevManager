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
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.BasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.MetaInfoBar
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PcBasicInfoForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PeDetailForm
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.PriceInfoForm
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 桌面布局（>840dp）：顶部作品信息条（只读元数据横排仪表盘式）+ 双栏并排编辑表单
 * （左基本信息主编辑区 / 右定价侧边配置），限宽 1200dp 居中，充分利用宽屏。
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
                    // 双栏并排：左基本信息（主编辑区）/ 右定价（侧边配置），同屏可见，充分利用宽屏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 左栏：基本信息 +（勾选同步生成 PC 模组时）PC 基本信息
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
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
                        }
                        // 右栏：定价
                        PriceInfoForm(
                            state = state,
                            onAction = onAction,
                            modifier = Modifier.weight(0.82f)
                        )
                    }
                }
            }
        }
    }
}
