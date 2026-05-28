package com.lemon.mcdevmanagermp.data.vo.netease.comment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentData(
    @SerialName("_id")
    val id: String,
    @SerialName("comment_tag")
    val commentTag: String,
    val iid: String,
    val nickname: String,
    @SerialName("publish_time")
    val publishTime: Long,
    @SerialName("res_name")
    val resName: String,
    val stars: String,
    val uid: String,
    @SerialName("user_comment")
    val userComment: String
)

@Serializable
data class CommentListVO(
    val count: Int,
    val data: List<CommentData>
)
