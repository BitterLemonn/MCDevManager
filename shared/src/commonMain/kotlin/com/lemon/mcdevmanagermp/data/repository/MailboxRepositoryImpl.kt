package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.MailboxApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.dto.netease.mailbox.DeleteMailDTO
import com.lemon.mcdevmanagermp.data.dto.netease.mailbox.ReadMailDTO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailContentVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailListVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.UnReadMailVO
import com.lemon.mcdevmanagermp.domain.mailbox.MailboxRepository
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler

class MailboxRepositoryImpl : MailboxRepository {
    companion object {
        val INSTANCE by lazy { MailboxRepositoryImpl() }
        private val mailboxApi = MailboxApi.INSTANCE
    }

    override suspend fun getUnReadCount(): NetworkState<UnReadMailVO> =
        UnifiedExceptionHandler.handleRequest { mailboxApi.getUnReadCount() }

    override suspend fun getMailList(
        start: Int,
        span: Int,
        initLoad: Boolean,
        key: String?,
        haveRead: Boolean?,
        mailType: String?
    ): NetworkState<MailListVO> =
        UnifiedExceptionHandler.handleRequest {
            mailboxApi.getMailList(start, span, initLoad, key, haveRead, mailType)
        }

    override suspend fun getMailContent(mailId: String): NetworkState<MailContentVO> =
        UnifiedExceptionHandler.handleRequest { mailboxApi.getMailContent(mailId) }

    override suspend fun deleteMail(mailIdList: List<String>): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest { mailboxApi.deleteMail(DeleteMailDTO(mailIdList)) }

    override suspend fun readMail(
        mailIdList: List<String>,
        readAll: Boolean
    ): NetworkState<NoNeedData> =
        UnifiedExceptionHandler.handleRequest {
            mailboxApi.readMail(ReadMailDTO(mailIdList = mailIdList, readAll = readAll))
        }
}
