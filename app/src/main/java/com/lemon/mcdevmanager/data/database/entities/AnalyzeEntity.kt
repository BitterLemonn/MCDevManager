package com.lemon.mcdevmanager.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class AnalyzeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val nickname: String,
    @ColumnInfo val filterType: Int,
    @ColumnInfo val platform: String,
    @ColumnInfo val startDate: String,
    @ColumnInfo val endDate: String,
    @ColumnInfo val filterResourceList: String,
    @ColumnInfo val createTime: Long = System.currentTimeMillis()
)
