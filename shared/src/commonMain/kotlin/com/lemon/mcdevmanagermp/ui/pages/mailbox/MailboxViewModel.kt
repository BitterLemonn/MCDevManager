package com.lemon.mcdevmanagermp.ui.pages.mailbox

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.MailboxRepositoryImpl
import com.lemon.mcdevmanagermp.domain.mailbox.MailboxUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class MailboxViewModel :
    BaseViewModel<MailboxState, MailboxAction, MailboxEffect>(MailboxState()) {

    private val mailboxUseCase = MailboxUseCase(MailboxRepositoryImpl.INSTANCE)

    override fun dispatch(action: MailboxAction) {
        when (action) {
            MailboxAction.LoadData -> loadMailList()
            MailboxAction.LoadMore -> loadMore()
            is MailboxAction.SelectMailType -> {
                setState { copy(selectedMailType = action.mailType) }
                loadMailList()
            }

            is MailboxAction.OpenMail -> openMail(action.mailId)
            MailboxAction.CloseDetail -> setState {
                copy(showDetail = false, currentMailMeta = null, currentMailContent = null)
            }

            is MailboxAction.DeleteMail -> deleteMail(action.mailId)
            MailboxAction.MarkAllRead -> markAllRead()
            MailboxAction.ShowDeleteReadConfirm -> setState { copy(showDeleteReadConfirm = true) }
            MailboxAction.DismissDeleteReadConfirm -> setState { copy(showDeleteReadConfirm = false) }
            MailboxAction.DeleteReadMails -> deleteReadMails()
        }
    }

    private fun loadMailList() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val r = mailboxUseCase.loadMailList(state.value.selectedMailType)) {
                is NetworkState.Success -> {
                    val newList = r.data?.mail ?: emptyList()
                    val total = r.data?.count ?: 0
                    setState {
                        copy(
                            isLoading = false,
                            mailList = newList,
                            totalCount = total,
                            unreadCount = r.data?.unreadCount ?: 0,
                            unreadMailCounts = r.data?.unreadMailCounts,
                            hasMore = newList.size >= MailboxUseCase.MAIL_PAGE_SIZE && newList.size < total
                        )
                    }
                }

                is NetworkState.Error -> {
                    setState { copy(isLoading = false) }
                    handleError(
                        r,
                        onNeedReLogin = { MailboxEffect.NeedReLogin },
                        onShowToast = { MailboxEffect.ShowToast("加载消息失败: $it") }
                    )
                }
            }
        }
    }

    /** 追加加载下一页：以当前列表大小为起点，追加到列表尾部。 */
    private fun loadMore() {
        val current = state.value
        if (!current.hasMore || current.isLoadingMore || current.isLoading) return
        setState { copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val r = mailboxUseCase.loadMailList(
                mailType = current.selectedMailType,
                start = current.mailList.size,
                initLoad = false
            )) {
                is NetworkState.Success -> {
                    val more = r.data?.mail ?: emptyList()
                    val newList = current.mailList + more
                    setState {
                        copy(
                            isLoadingMore = false,
                            mailList = newList,
                            hasMore = more.size >= MailboxUseCase.MAIL_PAGE_SIZE && newList.size < totalCount
                        )
                    }
                }

                is NetworkState.Error -> {
                    setState { copy(isLoadingMore = false) }
                    handleError(
                        r,
                        onNeedReLogin = { MailboxEffect.NeedReLogin },
                        onShowToast = { MailboxEffect.ShowToast("加载更多失败: $it") }
                    )
                }
            }
        }
    }

    private fun openMail(mailId: String) {
        val meta = state.value.mailList.find { it.id == mailId } ?: return
        setState {
            copy(
                showDetail = true,
                currentMailMeta = meta,
                currentMailContent = null,
                isDetailLoading = true
            )
        }
        viewModelScope.launch {
            when (val r = mailboxUseCase.getMailContent(mailId)) {
                is NetworkState.Success -> setState {
                    copy(isDetailLoading = false, currentMailContent = r.data)
                }

                is NetworkState.Error -> {
                    setState { copy(isDetailLoading = false) }
                    handleError(
                        r,
                        onNeedReLogin = { MailboxEffect.NeedReLogin },
                        onShowToast = { MailboxEffect.ShowToast("打开消息失败: $it") }
                    )
                }
            }
            // 乐观标记已读：后台 getMailContent 已自动标记已读，前端仅刷新 UI，无需调 readMail
            if (!meta.haveRead) {
                setState {
                    copy(
                        mailList = mailList.map { if (it.id == mailId) it.copy(haveRead = true) else it },
                        unreadCount = (unreadCount - 1).coerceAtLeast(0)
                    )
                }
            }
        }
    }

    private fun deleteMail(mailId: String) {
        setState { copy(isDeleting = true) }
        viewModelScope.launch {
            when (val r = mailboxUseCase.deleteMail(listOf(mailId))) {
                is NetworkState.Success -> {
                    setState {
                        copy(
                            isDeleting = false,
                            showDetail = false,
                            currentMailMeta = null,
                            currentMailContent = null
                        )
                    }
                    sendEffect(MailboxEffect.ShowToast("已删除"))
                    loadMailList()
                }

                is NetworkState.Error -> {
                    setState { copy(isDeleting = false) }
                    handleError(
                        r,
                        onNeedReLogin = { MailboxEffect.NeedReLogin },
                        onShowToast = { MailboxEffect.ShowToast("删除失败: $it") }
                    )
                }
            }
        }
    }

    private fun markAllRead() {
        setState { copy(isMarkingRead = true) }
        viewModelScope.launch {
            when (val r = mailboxUseCase.markAllRead()) {
                is NetworkState.Success -> {
                    setState {
                        copy(
                            isMarkingRead = false,
                            mailList = mailList.map { it.copy(haveRead = true) },
                            unreadCount = 0
                        )
                    }
                    sendEffect(MailboxEffect.ShowToast("已全部标记为已读"))
                }

                is NetworkState.Error -> {
                    setState { copy(isMarkingRead = false) }
                    handleError(
                        r,
                        onNeedReLogin = { MailboxEffect.NeedReLogin },
                        onShowToast = { MailboxEffect.ShowToast("操作失败: $it") }
                    )
                }
            }
        }
    }

    private fun deleteReadMails() {
        val readIds = state.value.mailList.filter { it.haveRead }.map { it.id }
        setState { copy(showDeleteReadConfirm = false) }
        if (readIds.isEmpty()) {
            sendEffect(MailboxEffect.ShowToast("没有已读消息可删除"))
            return
        }
        setState { copy(isDeletingRead = true) }
        viewModelScope.launch {
            when (val r = mailboxUseCase.deleteMail(readIds)) {
                is NetworkState.Success -> {
                    setState { copy(isDeletingRead = false) }
                    sendEffect(MailboxEffect.ShowToast("已删除 ${readIds.size} 条已读消息"))
                    loadMailList()
                }

                is NetworkState.Error -> {
                    setState { copy(isDeletingRead = false) }
                    handleError(
                        r,
                        onNeedReLogin = { MailboxEffect.NeedReLogin },
                        onShowToast = { MailboxEffect.ShowToast("删除失败: $it") }
                    )
                }
            }
        }
    }
}
