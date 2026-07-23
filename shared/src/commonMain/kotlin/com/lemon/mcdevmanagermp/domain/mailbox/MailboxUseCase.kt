package com.lemon.mcdevmanagermp.domain.mailbox

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailContentVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailListVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.UnReadMailVO

/**
 * 消息 UseCase：封装消息列表、详情、删除、全部已读、未读数逻辑
 */
class MailboxUseCase(
    private val mailboxRepository: MailboxRepository
) {
    companion object {
        /** 邮箱列表单页大小（与后端默认 span 对齐） */
        const val MAIL_PAGE_SIZE: Int = 20
    }

    /**
     * 加载消息列表，mailType 为 null/空字符串时表示全部。
     * - [start] 分页起点，0 为首屏；追加加载时传入当前列表大小。
     * - [initLoad] 是否为首屏加载（透传给后端，影响未读数等聚合字段）。
     */
    suspend fun loadMailList(
        mailType: String?,
        start: Int = 0,
        initLoad: Boolean = true
    ): NetworkState<MailListVO> =
        mailboxRepository.getMailList(
            start = start,
            span = MAIL_PAGE_SIZE,
            initLoad = initLoad,
            mailType = mailType?.takeIf { it.isNotBlank() }
        )

    suspend fun getMailContent(mailId: String): NetworkState<MailContentVO> =
        mailboxRepository.getMailContent(mailId)

    suspend fun deleteMail(mailIdList: List<String>): NetworkState<NoNeedData> =
        mailboxRepository.deleteMail(mailIdList)

    suspend fun markAllRead(): NetworkState<NoNeedData> =
        mailboxRepository.readMail(mailIdList = emptyList(), readAll = true)

    suspend fun getUnReadCount(): NetworkState<UnReadMailVO> =
        mailboxRepository.getUnReadCount()
}
