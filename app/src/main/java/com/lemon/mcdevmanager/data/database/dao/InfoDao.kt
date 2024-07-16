package com.lemon.mcdevmanager.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lemon.mcdevmanager.data.database.entities.AnalyzeEntity
import com.lemon.mcdevmanager.data.database.entities.OverviewEntity

@Dao
interface InfoDao {

    @Query("SELECT * FROM overviewEntity WHERE nickname = :nickname ORDER BY timestamp DESC LIMIT 1")
    fun getLatestOverviewByNickname(nickname: String): OverviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOverview(overviewEntity: OverviewEntity)

    @Query("DELETE FROM overviewEntity WHERE nickname = :nickname")
    fun deleteOverviewByNickname(nickname: String)

    @Query("SELECT * FROM analyzeEntity WHERE nickname = :nickname ORDER BY createTime DESC LIMIT 1")
    fun getLastAnalyzeParmaByNickname(nickname: String): AnalyzeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnalyzeParam(analyzeEntity: AnalyzeEntity)
}