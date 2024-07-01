package com.lemon.mcdevmanager.data.netease.feedback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackBean(
    @SerialName("_id")
    val id: String = "0",
    @SerialName("commit_nickname")
    val commitNickname: String = "",
    @SerialName("commit_uid")
    val commitUid: String = "",
    val content: String = "",
    @SerialName("create_time")
    val createTime: Long = 0,
    @SerialName("feedback_log_file")
    val feedbackLogFile: String = "",
    @SerialName("forbid_reply")
    val forbidReply: Boolean = false,
    @SerialName("have_log_file")
    val haveLogFile: Boolean = false,
    val iid: String = "",
    @SerialName("pic_list")
    val picList: List<String> = emptyList(),
    val reply: String? = null,
    @SerialName("res_name")
    val resName: String = "",
    val type: String = ""
)

@Serializable
data class FeedbackResponseBean(
    val data: List<FeedbackBean>,
    val count: Int
)

@Serializable
data class ConflictModBean(
    val iid: Long? = null,
    val name: String
)

@Serializable
data class ConflictModsBean(
    @SerialName("item_list")
    val itemList: List<ConflictModBean>,
    @SerialName("conflict_type")
    val conflictType: List<Int>,
    val detail: String? = null
)