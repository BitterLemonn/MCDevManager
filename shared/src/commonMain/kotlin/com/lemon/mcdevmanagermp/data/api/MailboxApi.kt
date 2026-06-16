package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.NoNeedData
import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.consts.NETEASE_MC_DEV_LINK
import com.lemon.mcdevmanagermp.data.dto.netease.mailbox.DeleteMailDTO
import com.lemon.mcdevmanagermp.data.dto.netease.mailbox.ReadMailDTO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailContentVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.MailListVO
import com.lemon.mcdevmanagermp.data.vo.netease.mailbox.UnReadMailVO
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface MailboxApi {

    @GET("/mailbox/unread/count")
    suspend fun getUnReadCount(): ResponseData<UnReadMailVO>

    @GET("/mailbox")
    suspend fun getMailList(
        @Query("start") start: Int = 0,
        @Query("span") span: Int = 20,
        @Query("initLoad") initLoad: Boolean = true,
        @Query("title") key: String? = null,
        @Query("have_read") haveRead: Boolean? = null,
        @Query("mail_type") mailType: String? = null,
    ): ResponseData<MailListVO>

    @GET("/mailbox/{mailId}")
    suspend fun getMailContent(@Path("mailId") mailId: String): ResponseData<MailContentVO>

    @POST("/mailbox/delete_many")
    suspend fun deleteMail(@Body content: DeleteMailDTO): ResponseData<NoNeedData>

    @POST("/mailbox/read_mail")
    suspend fun readMail(@Body content: ReadMailDTO): ResponseData<NoNeedData>

    companion object {
        val INSTANCE by lazy {
            ApiFactory.provideKtorfit(NETEASE_MC_DEV_LINK).createMailboxApi()
        }
    }
}

