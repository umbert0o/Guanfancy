package com.guanfancy.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.guanfancy.app.data.local.dao.IntakeDao
import com.guanfancy.app.data.local.entity.IntakeEntity

@Database(
    entities = [IntakeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun intakeDao(): IntakeDao
}
