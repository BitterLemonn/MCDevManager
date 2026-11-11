package com.lemon.mcdevmanagermp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lemon.mcdevmanagermp.data.db.entity.EcoFeeRecordEntity

@Dao
interface EcoFeeRecordDao {

    @Query("SELECT * FROM eco_fee_record WHERE accountKey = :accountKey AND dataMonth = :dataMonth LIMIT 1")
    suspend fun get(accountKey: String, dataMonth: String): EcoFeeRecordEntity?

    @Query("SELECT * FROM eco_fee_record WHERE accountKey = :accountKey AND ecoFeeRate IS NOT NULL ORDER BY dataMonth DESC")
    suspend fun getRatedByAccount(accountKey: String): List<EcoFeeRecordEntity>

    @Upsert
    suspend fun upsert(record: EcoFeeRecordEntity)
}
