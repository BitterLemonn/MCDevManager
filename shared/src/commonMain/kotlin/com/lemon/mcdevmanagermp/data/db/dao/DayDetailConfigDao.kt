package com.lemon.mcdevmanagermp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lemon.mcdevmanagermp.data.db.entity.DayDetailConfigEntity

@Dao
interface DayDetailConfigDao {

    @Query(
        """SELECT * FROM day_detail_config
           WHERE accountKey = :accountKey AND platform = :platform LIMIT 1"""
    )
    suspend fun getByKey(accountKey: String, platform: String): DayDetailConfigEntity?

    @Upsert
    suspend fun upsert(config: DayDetailConfigEntity)
}
