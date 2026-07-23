package com.lemon.mcdevmanagermp.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.lemon.mcdevmanagermp.data.db.dao.AccountDao
import com.lemon.mcdevmanagermp.data.db.dao.DayDetailConfigDao
import com.lemon.mcdevmanagermp.data.db.dao.PromotionTemplateDao
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.data.db.entity.DayDetailConfigEntity
import com.lemon.mcdevmanagermp.data.db.entity.PromotionTemplateEntity

@Database(
    entities = [AccountEntity::class, PromotionTemplateEntity::class, DayDetailConfigEntity::class],
    version = 6,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun promotionTemplateDao(): PromotionTemplateDao
    abstract fun dayDetailConfigDao(): DayDetailConfigDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

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
        connection.execSQL(
            "ALTER TABLE promotion_template ADD COLUMN promoImageUrl TEXT NOT NULL DEFAULT ''"
        )
    }
}
