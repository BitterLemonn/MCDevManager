package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.db.entity.ArchivedWorkEntity
import kotlin.time.Clock

/**
 * 本地作品归档仓储：负责按账号读写已归档的作品 ID 集合。
 */
interface ArchivedWorkRepository {
    suspend fun getArchivedItemIds(accountKey: String): Set<String>
    suspend fun archive(accountKey: String, itemId: String)
    suspend fun archiveAll(accountKey: String, itemIds: Collection<String>)
    suspend fun unarchive(accountKey: String, itemId: String)
    suspend fun unarchiveAll(accountKey: String, itemIds: Collection<String>)
}

class ArchivedWorkRepositoryImpl : ArchivedWorkRepository {
    companion object {
        val INSTANCE: ArchivedWorkRepository by lazy { ArchivedWorkRepositoryImpl() }
    }

    override suspend fun getArchivedItemIds(accountKey: String): Set<String> {
        if (accountKey.isBlank()) return emptySet()
        return AppContext.database.archivedWorkDao().getArchivedItemIds(accountKey).toSet()
    }

    override suspend fun archive(accountKey: String, itemId: String) {
        if (accountKey.isBlank() || itemId.isBlank()) return
        AppContext.database.archivedWorkDao().upsert(
            ArchivedWorkEntity(
                accountKey = accountKey,
                itemId = itemId,
                archivedAt = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun archiveAll(accountKey: String, itemIds: Collection<String>) {
        if (accountKey.isBlank()) return
        val validIds = itemIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (validIds.isEmpty()) return
        val now = Clock.System.now().toEpochMilliseconds()
        AppContext.database.archivedWorkDao().upsertAll(
            validIds.map { id ->
                ArchivedWorkEntity(
                    accountKey = accountKey,
                    itemId = id,
                    archivedAt = now
                )
            }
        )
    }

    override suspend fun unarchive(accountKey: String, itemId: String) {
        if (accountKey.isBlank() || itemId.isBlank()) return
        AppContext.database.archivedWorkDao().delete(accountKey, itemId)
    }

    override suspend fun unarchiveAll(accountKey: String, itemIds: Collection<String>) {
        if (accountKey.isBlank()) return
        val validIds = itemIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (validIds.isEmpty()) return
        AppContext.database.archivedWorkDao().deleteAll(accountKey, validIds)
    }
}
