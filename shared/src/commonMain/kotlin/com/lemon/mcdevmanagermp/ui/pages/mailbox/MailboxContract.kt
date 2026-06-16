package com.lemon.mcdevmanagermp.ui.pages.mailbox

import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailContentVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailCountsVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailListContentVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class MailboxState(
    val isLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
    val mailList: List<MailListContentVO> = emptyList(),
    val totalCount: Int = 0,
    val unreadCount: Int = 0,
    val unreadMailCounts: MailCountsVO? = null,
    val selectedMailType: String? = null,
    val currentMailMeta: MailListContentVO? = null,
    val currentMailContent: MailContentVO? = null,
    val showDetail: Boolean = false,
    val isDeleting: Boolean = false,
    val isMarkingRead: Boolean = false,
    val showDeleteReadConfirm: Boolean = false,
    val isDeletingRead: Boolean = false
) : IUiState {
    val hasUnread: Boolean get() = unreadCount > 0
    val hasReadMails: Boolean get() = mailList.any { it.haveRead }
}

sealed interface MailboxAction : IUiAction {
    data object LoadData : MailboxAction
    data class SelectMailType(val mailType: String?) : MailboxAction
    data class OpenMail(val mailId: String) : MailboxAction
    data object CloseDetail : MailboxAction
    data class DeleteMail(val mailId: String) : MailboxAction
    data object MarkAllRead : MailboxAction
    data object ShowDeleteReadConfirm : MailboxAction
    data object DismissDeleteReadConfirm : MailboxAction
    data object DeleteReadMails : MailboxAction
}

sealed interface MailboxEffect : IUiEffect {
    data class ShowToast(val message: String) : MailboxEffect
    data object NeedReLogin : MailboxEffect
}

/**
 * 消息类型筛选选项。key 与 [MailCountsVO] 的 @SerialName 对应（联调时需校正）
 */
data class MailTypeOption(val key: String?, val label: String)

val MAIL_TYPE_OPTIONS: List<MailTypeOption> = listOf(
    MailTypeOption(null, "全部"),
    MailTypeOption("system_notice", "系统通知"),
    MailTypeOption("important_notice", "重要通知"),
    MailTypeOption("review_notice", "审核通知"),
    MailTypeOption("issue_feedback", "问题反馈"),
    MailTypeOption("notify", "提醒")
)

/** 将 mailType key 转为中文标签，未命中时原样返回 */
fun mailTypeLabel(key: String): String =
    MAIL_TYPE_OPTIONS.firstOrNull { it.key == key }?.label ?: key.ifBlank { "通知" }
