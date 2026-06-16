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
    /** 加载消息列表，mailType 为 null/空字符串时表示全部 */
    suspend fun loadMailList(mailType: String?): NetworkState<MailListVO> =
        mailboxRepository.getMailList(mailType = mailType?.takeIf { it.isNotBlank() })

    suspend fun getMailContent(mailId: String): NetworkState<MailContentVO> =
        mailboxRepository.getMailContent(mailId)

    suspend fun deleteMail(mailIdList: List<String>): NetworkState<NoNeedData> =
        mailboxRepository.deleteMail(mailIdList)

    /** 全部已读 */
    suspend fun markAllRead(): NetworkState<NoNeedData> =
        mailboxRepository.readMail(mailIdList = emptyList(), readAll = true)

    suspend fun getUnReadCount(): NetworkState<UnReadMailVO> =
        mailboxRepository.getUnReadCount()
}
