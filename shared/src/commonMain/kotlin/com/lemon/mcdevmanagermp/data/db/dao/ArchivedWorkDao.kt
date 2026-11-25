package com.lemon.mcdevmanagermp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lemon.mcdevmanagermp.data.db.entity.ArchivedWorkEntity

@Dao
interface ArchivedWorkDao {

    @Query("SELECT itemId FROM archived_work WHERE accountKey = :accountKey ORDER BY archivedAt DESC")
    suspend fun getArchivedItemIds(accountKey: String): List<String>

    @Upsert
    suspend fun upsert(entity: ArchivedWorkEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ArchivedWorkEntity>)

    @Query("DELETE FROM archived_work WHERE accountKey = :accountKey AND itemId = :itemId")
    suspend fun delete(accountKey: String, itemId: String)

    @Query("DELETE FROM archived_work WHERE accountKey = :accountKey AND itemId IN (:itemIds)")
    suspend fun deleteAll(accountKey: String, itemIds: List<String>)
}
