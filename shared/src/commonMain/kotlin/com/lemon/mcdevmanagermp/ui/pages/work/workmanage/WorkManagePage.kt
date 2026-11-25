package com.lemon.mcdevmanagermp.ui.pages.work.workmanage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.data.consts.enums.WorkItemActionEnum
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.components.LoadingDialog
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.WorkManageCompactLayout
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.WorkManageExpandedLayout
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.WorkManageMediumLayout
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.AddArchiveDialog
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.FeedbackDialog
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManageActionDialog
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component.WorkManagePendingOp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun WorkManagePage(
    onBack: () -> Unit,
    onNeedReLogin: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToViewDetail: (String) -> Unit = {}
) {
    val viewModel = remember { WorkManageViewModel() }
    val state by viewModel.state.collectAsState()
    val colors = LocalAppColors.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var pending by remember { mutableStateOf<WorkManagePendingOp?>(null) }
    var showAddArchiveDialog by remember { mutableStateOf(false) }

    val handleCardAction: (ResourceData, WorkItemActionEnum) -> Unit = { item, action ->
        when (action) {
            WorkItemActionEnum.UPDATE -> onNavigateToDetail(item.itemId)
            WorkItemActionEnum.VIEW_DETAIL -> onNavigateToViewDetail(item.itemId)
            WorkItemActionEnum.SUBMIT_SELF_TEST ->
                pending = WorkManagePendingOp.SubmitSelfTest(item)

            WorkItemActionEnum.ADJUST_PRICE ->
                pending = WorkManagePendingOp.AdjustPrice(item)

            WorkItemActionEnum.APPOINT_ONLINE ->
                pending = WorkManagePendingOp.AppointOnline(item)

            WorkItemActionEnum.VIEW_FEEDBACK ->
                viewModel.dispatch(WorkManageAction.LoadFeedback(item))

            else -> pending = WorkManagePendingOp.Confirm(item, action)
        }
    }

    val handleToggleArchive: (ResourceData) -> Unit = { item ->
        if (item.itemId in state.archivedItemIds) {
            viewModel.dispatch(WorkManageAction.UnarchiveItem(item))
        } else {
            viewModel.dispatch(WorkManageAction.ArchiveItem(item))
        }
    }

    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is WorkManageEffect.ShowToast -> showToast(effect.message)
            WorkManageEffect.NeedReLogin -> onNeedReLogin()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(WorkManageAction.LoadData)
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthSizeClass = LocalWindowWidthSizeClass.current
        when (widthSizeClass) {
            WindowWidthSizeClass.Expanded -> WorkManageExpandedLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                onAddWork = { onNavigateToDetail("") },
                onCardAction = handleCardAction,
                onToggleArchive = handleToggleArchive,
                onOpenAddArchive = { showAddArchiveDialog = true },
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )

            WindowWidthSizeClass.Medium -> WorkManageMediumLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                onAddWork = { onNavigateToDetail("") },
                onCardAction = handleCardAction,
                onToggleArchive = handleToggleArchive,
                onOpenAddArchive = { showAddArchiveDialog = true },
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )

            else -> WorkManageCompactLayout(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                onAddWork = { onNavigateToDetail("") },
                onCardAction = handleCardAction,
                onToggleArchive = handleToggleArchive,
                onOpenAddArchive = { showAddArchiveDialog = true },
                statusBarTop = statusBarTop,
                navBarBottom = navBarBottom
            )
        }

        // 操作确认 / 自测 / 调价 / 定时上架弹窗
        WorkManageActionDialog(
            pending = pending,
            onConfirmAction = { item, action ->
                viewModel.dispatch(WorkManageAction.PerformAction(item, action))
                pending = null
            },
            onSubmitSelfTest = { item, passCheck ->
                viewModel.dispatch(WorkManageAction.SubmitSelfTest(item, passCheck))
                pending = null
            },
            onAdjustPrice = { item, newPrice ->
                viewModel.dispatch(WorkManageAction.AdjustPrice(item, newPrice))
                pending = null
            },
            onAppointOnline = { item, time ->
                viewModel.dispatch(WorkManageAction.AppointOnline(item, time))
                pending = null
            },
            onDismiss = { pending = null }
        )

        // 批量添加归档弹窗
        if (showAddArchiveDialog) {
            AddArchiveDialog(
                candidates = state.activeItems,
                onConfirm = { selectedIds ->
                    viewModel.dispatch(WorkManageAction.ArchiveItems(selectedIds))
                    showAddArchiveDialog = false
                },
                onDismiss = { showAddArchiveDialog = false }
            )
        }

        // 审核反馈弹窗：由 ViewModel 的 feedback 状态驱动，统一在一处渲染
        state.feedback?.let { fb ->
            FeedbackDialog(
                itemName = state.feedbackItemName,
                feedback = fb,
                onDismiss = { viewModel.dispatch(WorkManageAction.DismissFeedback) }
            )
        }

        // 写操作（提交审核/上架/定时上架等）期间的阻塞式等待弹窗
        if (state.isPerforming) {
            LoadingDialog(state.performingMessage)
        }
    }
}
