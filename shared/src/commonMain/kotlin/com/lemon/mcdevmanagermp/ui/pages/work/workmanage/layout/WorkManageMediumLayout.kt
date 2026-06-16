package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.ui.components.CollapsingTopBar
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageAction
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkManageState
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageActionDialog
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageCard
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManagePendingOp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun WorkManageMediumLayout(
    state: WorkManageState,
    onAction: (WorkManageAction) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    val gridState = rememberLazyGridState()
    var pending by remember { mutableStateOf<WorkManagePendingOp?>(null) }

    val scrollAlpha = remember {
        derivedStateOf {
            if (gridState.firstVisibleItemIndex > 0) 1f
            else (gridState.firstVisibleItemScrollOffset / 100f).coerceIn(0f, 1f)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CollapsingTopBar(
            title = "上架管理",
            collapseFraction = scrollAlpha.value,
            onBack = onBack,
            actions = {
                IconButton(onClick = { onAction(WorkManageAction.RefreshData) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = "刷新",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        when {
            state.isLoading && state.items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = colors.primary,
                    strokeWidth = 3.dp
                )
            }

            state.items.isEmpty() && !state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📦", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text = "暂无作品",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.itemId }) { item ->
                    WorkManageCard(item) { action ->
                        pending = if (action == WorkItemActionEnum.ADJUST_PRICE) {
                            WorkManagePendingOp.AdjustPrice(item)
                        } else {
                            WorkManagePendingOp.Confirm(item, action)
                        }
                    }
                }
            }
        }
    }

    WorkManageActionDialog(
        pending = pending,
        onConfirmAction = { item, action ->
            onAction(WorkManageAction.PerformAction(item, action))
            pending = null
        },
        onAdjustPrice = { item, newPrice ->
            onAction(WorkManageAction.AdjustPrice(item, newPrice))
            pending = null
        },
        onDismiss = { pending = null }
    )
}
