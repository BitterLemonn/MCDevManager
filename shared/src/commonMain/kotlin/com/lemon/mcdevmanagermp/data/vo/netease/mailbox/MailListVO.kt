package com.lemon.mcdevmanagermp.data.vo.netease.mailbox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MailListVO(
    val count: Int = 0,
    @SerialName("feedback_count")
    val feedbackCount: Int = 0,
    @SerialName("feedback_read_count")
    val feedbackReadCount: Int = 0,
    @SerialName("feedback_unread_count")
    val feedbackUnreadCount: Int = 0,
    val mail: List<MailListContentVO> = emptyList(),
    @SerialName("mail_counts")
    val mailCounts: MailCountsVO = MailCountsVO(),
    @SerialName("notice_count")
    val noticeCount: Int = 0,
    @SerialName("notice_read_count")
    val noticeReadCount: Int = 0,
    @SerialName("read_count")
    val readCount: Int = 0,
    @SerialName("unread_count")
    val unreadCount: Int = 0,
    @SerialName("unread_mail_counts")
    val unreadMailCounts: MailCountsVO = MailCountsVO()
)

@Serializable
data class MailCountsVO(
    @SerialName("important_notice")
    val importantNotice: Int = 0,
    @SerialName("issue_feedback")
    val issueFeedback: Int = 0,
    val notify: Int = 0,
    @SerialName("review_notice")
    val reviewNotice: Int = 0,
    @SerialName("system_notice")
    val systemNotice: Int = 0
)

@Serializable
data class MailListContentVO(
    @SerialName("_id")
    val id: String = "",
    @SerialName("have_read")
    val haveRead: Boolean = true,
    @SerialName("mail_type")
    val mailType: String = "",
    val time: String = "",
    val title: String = ""
)
