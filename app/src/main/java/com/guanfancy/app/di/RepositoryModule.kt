package com.guanfancy.app.di

import com.guanfancy.app.data.preferences.AppPreferences
import com.guanfancy.app.data.repository.MedicationRepositoryImpl
import com.guanfancy.app.data.repository.SettingsRepositoryImpl
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import com.guanfancy.app.domain.service.FoodZoneService
import com.guanfancy.app.domain.service.FoodZoneServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(
        medicationRepositoryImpl: MedicationRepositoryImpl
    ): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindFoodZoneService(
        foodZoneServiceImpl: FoodZoneServiceImpl
    ): FoodZoneService
}
