package com.guanfancy.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.guanfancy.app.data.local.dao.IntakeDao
import com.guanfancy.app.data.local.entity.IntakeEntity

@Database(
    entities = [IntakeEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun intakeDao(): IntakeDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medication_intakes ADD COLUMN source TEXT NOT NULL DEFAULT 'SCHEDULED'")
            }
        }
    }
}
