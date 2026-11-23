package com.lemon.mcdevmanagermp.data.db

import androidx.room.Room
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.data.db.entity.EcoFeeRecordEntity
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppDatabaseMigrationTest {

    @Test
    fun v9UpgradeClearsEcoFeeRecordsAndPreservesAccounts() = runBlocking {
        val tempDirectory = Files.createTempDirectory("mcdev-db-migration-").toFile()
        try {
            val dbPath = tempDirectory.resolve("migration.db").absolutePath
            val driver = BundledSQLiteDriver()
            driver.open(dbPath).use { connection -> createV9Database(connection) }

            val database = Room.databaseBuilder<AppDatabase>(
                name = dbPath,
                factory = AppDatabaseConstructor::initialize
            ).setDriver(driver)
                .addAppDatabaseMigrations()
                .build()
            try {
                assertEquals(
                    listOf(AccountEntity(id = 1, nickname = "test-account", cookiesJson = "{}", lastLoginTime = 123)),
                    database.accountDao().getAllAccounts()
                )

                val feeDao = database.ecoFeeRecordDao()
                assertNull(feeDao.get("test-account", "2026-09"))
                assertNull(feeDao.get("test-account", "2026-10"))

                val recalculated = EcoFeeRecordEntity(
                    accountKey = "test-account",
                    dataMonth = "2026-09",
                    profitDataJson = "{}",
                    ecoFeeRate = 0.3,
                    updatedAt = 456
                )
                feeDao.upsert(recalculated)
                assertEquals(recalculated, feeDao.get("test-account", "2026-09"))
            } finally {
                database.close()
            }
        } finally {
            tempDirectory.deleteRecursively()
        }
    }

    private fun createV9Database(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE account (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nickname TEXT NOT NULL,
                cookiesJson TEXT NOT NULL, lastLoginTime INTEGER NOT NULL, headImg TEXT,
                email TEXT NOT NULL DEFAULT '', password TEXT NOT NULL DEFAULT '',
                rememberPassword INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE promotion_template (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL,
                extra TEXT NOT NULL, activity TEXT NOT NULL, feature TEXT NOT NULL,
                updateContent TEXT NOT NULL, promoImageUrl TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE day_detail_config (
                accountKey TEXT NOT NULL, platform TEXT NOT NULL, dateSpanDays INTEGER NOT NULL,
                selectedIIDs TEXT NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY (accountKey, platform)
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE eco_fee_record (
                accountKey TEXT NOT NULL, dataMonth TEXT NOT NULL, profitDataJson TEXT NOT NULL,
                ecoFeeRate REAL, ecoFeeAmount REAL, actualIncome REAL, playPlanIncome REAL,
                updatedAt INTEGER NOT NULL, PRIMARY KEY (accountKey, dataMonth)
            )
            """.trimIndent()
        )
        connection.execSQL(
            "INSERT INTO account (id, nickname, cookiesJson, lastLoginTime) VALUES (1, 'test-account', '{}', 123)"
        )
        connection.execSQL(
            """
            INSERT INTO eco_fee_record (accountKey, dataMonth, profitDataJson, ecoFeeRate, updatedAt)
            VALUES ('test-account', '2026-09', '{}', 0.92, 123), ('test-account', '2026-10', '{}', NULL, 123)
            """.trimIndent()
        )
        connection.execSQL("PRAGMA user_version = 9")
    }
}
