package com.lemon.mcdevmanagermp.domain.mailbox

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailContentVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailListVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.UnReadMailVO

interface MailboxRepository {
    suspend fun getUnReadCount(): NetworkState<UnReadMailVO>

    suspend fun getMailList(
        start: Int = 0,
        span: Int = 20,
        initLoad: Boolean = true,
        key: String? = null,
        haveRead: Boolean? = null,
        mailType: String? = null
    ): NetworkState<MailListVO>

    suspend fun getMailContent(mailId: String): NetworkState<MailContentVO>

    suspend fun deleteMail(mailIdList: List<String>): NetworkState<NoNeedData>

    suspend fun readMail(
        mailIdList: List<String> = emptyList(),
        readAll: Boolean = false
    ): NetworkState<NoNeedData>
}
