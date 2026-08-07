package com.lemon.mcdevmanagermp.data.vo.netease.feedback

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NullableStringListSerializer : KSerializer<List<String>> {
    private val delegate = ListSerializer(String.serializer().nullable)

    override val descriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): List<String> =
        delegate.deserialize(decoder).filterNotNull()

    override fun serialize(encoder: Encoder, value: List<String>) =
        delegate.serialize(encoder, value)
}

@Serializable
data class FeedbackData(
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
    @Serializable(with = NullableStringListSerializer::class)
    val picList: List<String> = emptyList(),
    val reply: String? = null,
    @SerialName("res_name")
    val resName: String = "",
    val type: String = ""
)

@Serializable
data class FeedbackVO(
    val data: List<FeedbackData>,
    val count: Int
)

@Serializable
data class ConflictModData(
    val iid: Long? = null,
    val name: String
)

@Serializable
data class ConflictModsVO(
    @SerialName("item_list")
    val itemList: List<ConflictModData>,
    @SerialName("conflict_type")
    val conflictType: List<Int>,
    val detail: String? = null
)
