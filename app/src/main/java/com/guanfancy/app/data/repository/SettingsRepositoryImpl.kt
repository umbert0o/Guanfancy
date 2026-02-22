package com.guanfancy.app.data.repository

import com.guanfancy.app.data.preferences.AppPreferences
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.MedicationType
import com.guanfancy.app.domain.model.ScheduleConfig
import com.guanfancy.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences
) : SettingsRepository {

    override val isWarningAccepted: Flow<Boolean> = appPreferences.isWarningAccepted

    override val isOnboardingCompleted: Flow<Boolean> = appPreferences.isOnboardingCompleted

    override val medicationType: Flow<MedicationType> = appPreferences.medicationType

    override val foodZoneConfig: Flow<FoodZoneConfig> = appPreferences.medicationType.map { type ->
        type.getFoodZoneConfig()
    }

    override val scheduleConfig: Flow<ScheduleConfig> = appPreferences.scheduleConfig

    override val currentIntakeTime: Flow<Instant?> = appPreferences.currentIntakeTime.map { epoch ->
        epoch?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        appPreferences.setOnboardingCompleted(completed)
    }

    override suspend fun setMedicationType(type: MedicationType) {
        appPreferences.setMedicationType(type)
    }

    override suspend fun updateScheduleConfig(config: ScheduleConfig) {
        appPreferences.updateScheduleConfig(config)
    }

    override suspend fun setCurrentIntakeTime(time: Instant) {
        appPreferences.setCurrentIntakeTime(time.toEpochMilliseconds())
    }

    override suspend fun setWarningAccepted(accepted: Boolean) {
        appPreferences.setWarningAccepted(accepted)
    }

    override suspend fun clearAllData() {
        appPreferences.clearAllData()
    }
}
