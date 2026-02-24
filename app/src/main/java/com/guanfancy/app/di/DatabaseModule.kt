package com.guanfancy.app.di

import android.content.Context
import androidx.room.Room
import com.guanfancy.app.data.local.AppDatabase
import com.guanfancy.app.data.local.dao.IntakeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "guanfancy_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideIntakeDao(database: AppDatabase): IntakeDao {
        return database.intakeDao()
    }
}
