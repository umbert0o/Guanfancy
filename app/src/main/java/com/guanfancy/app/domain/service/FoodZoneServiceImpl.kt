package com.guanfancy.app.domain.service

import com.guanfancy.app.domain.model.FoodZone
import com.guanfancy.app.domain.model.FoodZoneCalculator
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodZoneServiceImpl @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val settingsRepository: SettingsRepository
) : FoodZoneService {

    override fun getCurrentZone(): Flow<FoodZone> {
        return combine(
            medicationRepository.getLastCompletedIntakeFlow(),
            medicationRepository.getNextScheduledIntakeFlow(),
            settingsRepository.foodZoneConfig
        ) { lastCompleted, nextScheduled, config ->
            calculateCurrentZone(
                lastCompletedIntake = lastCompleted,
                nextScheduledIntake = nextScheduled,
                config = config
            )
        }
    }

    private fun calculateCurrentZone(
        lastCompletedIntake: MedicationIntake?,
        nextScheduledIntake: MedicationIntake?,
        config: FoodZoneConfig
    ): FoodZone {
        val now = Clock.System.now()
        
        return FoodZoneCalculator.calculate(
            now = now,
            lastTakenTime = lastCompletedIntake?.actualTime,
            nextScheduledTime = nextScheduledIntake?.scheduledTime,
            config = config
        )
    }
}
