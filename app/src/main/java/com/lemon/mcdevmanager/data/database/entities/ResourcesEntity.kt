package com.lemon.mcdevmanager.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lemon.mcdevmanager.data.netease.resource.ResourceBean
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class ResourcesEntity(
    @PrimaryKey(autoGenerate = true) private val _id: Int = 0,
    @ColumnInfo val nickname: String = "",
    @ColumnInfo val resList: List<ResourceBean>,
    @ColumnInfo val resCount: Int = resList.count()
)