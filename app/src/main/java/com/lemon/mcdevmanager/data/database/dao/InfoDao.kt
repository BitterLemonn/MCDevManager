package com.lemon.mcdevmanager.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lemon.mcdevmanager.data.database.entities.OverviewEntity

@Dao
interface InfoDao {

    @Query("SELECT * FROM overviewEntity WHERE nickname = :nickname ORDER BY timestamp DESC LIMIT 1")
    fun getLatestOverviewByNickname(nickname: String): OverviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOverview(overviewEntity: OverviewEntity)
}