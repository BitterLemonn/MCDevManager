package com.lemon.mcdevmanagermp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lemon.mcdevmanagermp.data.db.entity.PromotionTemplateEntity

@Dao
interface PromotionTemplateDao {

    @Query("SELECT * FROM promotion_template ORDER BY createdAt DESC")
    suspend fun getAll(): List<PromotionTemplateEntity>

    @Upsert
    suspend fun upsert(template: PromotionTemplateEntity)

    @Query("DELETE FROM promotion_template WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM promotion_template WHERE name = :name")
    suspend fun deleteByName(name: String)
}
