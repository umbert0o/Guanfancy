package com.guanfancy.app.domain.repository

import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.MedicationType
import com.guanfancy.app.domain.model.ScheduleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface SettingsRepository {
    val isWarningAccepted: Flow<Boolean>
    val isOnboardingCompleted: Flow<Boolean>
    val medicationType: Flow<MedicationType>
    val foodZoneConfig: Flow<FoodZoneConfig>
    val scheduleConfig: Flow<ScheduleConfig>
    val currentIntakeTime: Flow<Instant?>

    suspend fun setWarningAccepted(accepted: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setMedicationType(type: MedicationType)
    suspend fun updateScheduleConfig(config: ScheduleConfig)
    suspend fun setCurrentIntakeTime(time: Instant)
    suspend fun clearAllData()
}
