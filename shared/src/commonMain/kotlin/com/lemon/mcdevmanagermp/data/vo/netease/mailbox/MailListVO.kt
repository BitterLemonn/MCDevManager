package com.lemon.mcdevmanagermp.data.vo.netease.mailbox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MailListVO(
    val count: Int,
    @SerialName("feedback_count")
    val feedbackCount: Int,
    @SerialName("feedback_read_count")
    val feedbackReadCount: Int,
    @SerialName("feedback_unread_count")
    val feedbackUnreadCount: Int,
    val mail: List<MailListContentVO>,
    @SerialName("mail_counts")
    val mailCounts: MailCountsVO,
    @SerialName("notice_count")
    val noticeCount: Int,
    @SerialName("notice_read_count")
    val noticeReadCount: Int,
    @SerialName("read_count")
    val readCount: Int,
    @SerialName("unread_count")
    val unreadCount: Int,
    @SerialName("unread_mail_counts")
    val unreadMailCounts: MailCountsVO
)

@Serializable
data class MailCountsVO(
    @SerialName("important_notice")
    val importantNotice: Int,
    @SerialName("issue_feedback")
    val issueFeedback: Int,
    val notify: Int,
    @SerialName("review_notice")
    val reviewNotice: Int,
    @SerialName("system_notice")
    val systemNotice: Int
)

@Serializable
data class MailListContentVO(
    @SerialName("_id")
    val id: String,
    @SerialName("have_read")
    val haveRead: Boolean,
    @SerialName("mail_type")
    val mailType: String,
    val time: String,
    val title: String
)