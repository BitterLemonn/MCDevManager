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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailAction
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.WorkDetailState
import com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component.MetaInfoBar
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/** 桌面布局（>=840dp）：顶部作品摘要 + 等宽双栏，内容限宽 1200dp。 */
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

    Column(modifier = Modifier.fillMaxSize()) {
        CollapsingTopBar(
            title = if (state.detail == null) "新建作品" else "作品详情",
            collapseFraction = scrollAlpha.value,
            onBack = onBack,
            actions = {
                WorkDetailTopBarActions(
                    isSubmitting = state.isSubmitting,
                    onAction = onAction
                )
            }
        )

        if (state.isLoading && state.detail == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = colors.primary,
                    strokeWidth = 3.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 1200.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (state.detail != null) {
                        MetaInfoBar(state = state)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        WorkDetailInfoSections(
                            state = state,
                            onAction = onAction,
                            columns = 2,
                            showMetaRow = false,
                            modifier = Modifier.weight(1f)
                        )
                        WorkDetailMediaSections(
                            state = state,
                            onAction = onAction,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
