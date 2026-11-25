package com.lemon.mcdevmanagermp.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.lemon.mcdevmanagermp.data.db.dao.AccountDao
import com.lemon.mcdevmanagermp.data.db.dao.ArchivedWorkDao
import com.lemon.mcdevmanagermp.data.db.dao.DayDetailConfigDao
import com.lemon.mcdevmanagermp.data.db.dao.EcoFeeRecordDao
import com.lemon.mcdevmanagermp.data.db.dao.PromotionTemplateDao
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.data.db.entity.ArchivedWorkEntity
import com.lemon.mcdevmanagermp.data.db.entity.DayDetailConfigEntity
import com.lemon.mcdevmanagermp.data.db.entity.EcoFeeRecordEntity
import com.lemon.mcdevmanagermp.data.db.entity.PromotionTemplateEntity

@Database(
    entities = [
        AccountEntity::class,
        PromotionTemplateEntity::class,
        DayDetailConfigEntity::class,
        EcoFeeRecordEntity::class,
        ArchivedWorkEntity::class
    ],
    version = 11,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun promotionTemplateDao(): PromotionTemplateDao
    abstract fun dayDetailConfigDao(): DayDetailConfigDao
    abstract fun ecoFeeRecordDao(): EcoFeeRecordDao
    abstract fun archivedWorkDao(): ArchivedWorkDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

internal fun RoomDatabase.Builder<AppDatabase>.addAppDatabaseMigrations() = addMigrations(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11
)

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE account ADD COLUMN headImg TEXT DEFAULT NULL")
    }
}

/**
 * 数据库迁移 2→3：将 email 列重命名为 nickname，语义从邮箱改为昵称标识
 * 运行时 MainViewModel/AutoLoginUseCase 获取 userInfo 后会更新 nickname 字段值
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE account RENAME COLUMN email TO nickname")
    }
}

/**
 * 数据库迁移 3→4：新增 PE 轮播图申请文案模板表。
 * updateContent 列名避开 SQL 保留字 UPDATE（domain 层字段名为 update）。
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS promotion_template (
              id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              name TEXT NOT NULL,
              extra TEXT NOT NULL,
              activity TEXT NOT NULL,
              feature TEXT NOT NULL,
              updateContent TEXT NOT NULL,
              createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

/**
 * 数据库迁移 4→5：新增数据追踪页「上次查询配置」表（按账号 + 平台隔离）。
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS day_detail_config (
              accountKey TEXT NOT NULL,
              platform TEXT NOT NULL,
              dateSpanDays INTEGER NOT NULL,
              selectedIIDs TEXT NOT NULL,
              updatedAt INTEGER NOT NULL,
              PRIMARY KEY (accountKey, platform)
            )
            """.trimIndent()
        )
    }
}

/**
 * 数据库迁移 5→6：promotion_template 表新增 promoImageUrl 列（PE 轮播图预览图 URL）。
 * Entity 该列声明 @ColumnInfo(defaultValue = "")，与此处 DEFAULT '' 对齐，确保 Room schema 校验通过。
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        val columnExists = connection.prepare(
            "SELECT COUNT(*) FROM pragma_table_info('promotion_template') WHERE name = 'promoImageUrl'"
        ).use { stmt ->
            stmt.step()
            stmt.getLong(0) > 0
        }
        if (!columnExists) {
            connection.execSQL(
                "ALTER TABLE promotion_template ADD COLUMN promoImageUrl TEXT NOT NULL DEFAULT ''"
            )
        }
    }
}

/**
 * 数据库迁移 6→7：account 表新增 email、password、rememberPassword 列（用于记住密码与多账号切换填充）。
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        val emailExists = connection.prepare(
            "SELECT COUNT(*) FROM pragma_table_info('account') WHERE name = 'email'"
        ).use { stmt ->
            stmt.step()
            stmt.getLong(0) > 0
        }
        if (!emailExists) {
            connection.execSQL(
                "ALTER TABLE account ADD COLUMN email TEXT NOT NULL DEFAULT ''"
            )
        }
        val passwordExists = connection.prepare(
            "SELECT COUNT(*) FROM pragma_table_info('account') WHERE name = 'password'"
        ).use { stmt ->
            stmt.step()
            stmt.getLong(0) > 0
        }
        if (!passwordExists) {
            connection.execSQL(
                "ALTER TABLE account ADD COLUMN password TEXT NOT NULL DEFAULT ''"
            )
        }
        val rememberPasswordExists = connection.prepare(
            "SELECT COUNT(*) FROM pragma_table_info('account') WHERE name = 'rememberPassword'"
        ).use { stmt ->
            stmt.step()
            stmt.getLong(0) > 0
        }
        if (!rememberPasswordExists) {
            connection.execSQL(
                "ALTER TABLE account ADD COLUMN rememberPassword INTEGER NOT NULL DEFAULT 0"
            )
        }
    }
}

/**
 * 数据库迁移 7→8：新增每月生态费记录表（按账号 + 月份隔离）。
 * 周期截止后保存预估快照，出账后补全费率字段（可空）。
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(CREATE_ECO_FEE_RECORD_TABLE_SQL)
    }
}

/**
 * 数据库迁移 8→9：重建 eco_fee_record 表。
 * 早期 8 版本的表结构（费率字段非空、无快照列）与最终结构不一致，直接重建；
 * 旧表数据不迁移，快照可由后续登录重新保存，费率在结算单出账后浏览时重新反推。
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS eco_fee_record")
        connection.execSQL(CREATE_ECO_FEE_RECORD_TABLE_SQL)
    }
}

/**
 * 数据库迁移 9→10：重建 eco_fee_record 表。
 * 9 版本把绿宝石计价模组的商品流水（平台允许其改为钻石模组后按钻石面额折算）按钻石流水
 * 计入了反推分母，费率普遍虚高，旧记录全部作废；表结构与 8→9 一致，仅清空数据，
 * 快照与费率由后续登录按正确口径重新生成。
 */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS eco_fee_record")
        connection.execSQL(CREATE_ECO_FEE_RECORD_TABLE_SQL)
    }
}

/**
 * 数据库迁移 10→11：新增本地作品归档表（按账号 + 作品 ID 隔离）。
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS archived_work (
              accountKey TEXT NOT NULL,
              itemId TEXT NOT NULL,
              archivedAt INTEGER NOT NULL,
              PRIMARY KEY (accountKey, itemId)
            )
            """.trimIndent()
        )
    }
}

private const val CREATE_ECO_FEE_RECORD_TABLE_SQL = """
    CREATE TABLE IF NOT EXISTS eco_fee_record (
      accountKey TEXT NOT NULL,
      dataMonth TEXT NOT NULL,
      profitDataJson TEXT NOT NULL,
      ecoFeeRate REAL,
      ecoFeeAmount REAL,
      actualIncome REAL,
      playPlanIncome REAL,
      updatedAt INTEGER NOT NULL,
      PRIMARY KEY (accountKey, dataMonth)
    )
"""
