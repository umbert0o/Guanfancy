package com.guanfancy.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guanfancy.app.domain.model.FoodZone
import com.guanfancy.app.domain.model.FoodZoneCalculator
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.model.ScheduleConfig
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import javax.inject.Inject

data class DashboardState(
    val nextIntake: MedicationIntake? = null,
    val scheduleConfig: ScheduleConfig = ScheduleConfig.DEFAULT,
    val foodZoneConfig: FoodZoneConfig = FoodZoneConfig.DEFAULT,
    val currentFoodZone: FoodZone = FoodZone.GREEN,
    val timeUntilIntake: Long = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                medicationRepository.getAllIntakes(),
                settingsRepository.scheduleConfig,
                settingsRepository.foodZoneConfig
            ) { intakes, config, foodZoneConfig ->
                val nextIntake = intakes.firstOrNull { !it.isCompleted }
                val lastCompletedIntake = intakes.firstOrNull { it.isCompleted && it.actualTime != null }
                val now = Clock.System.now()

                val foodZone = FoodZoneCalculator.calculate(
                    now = now,
                    lastTakenTime = lastCompletedIntake?.actualTime,
                    nextScheduledTime = nextIntake?.scheduledTime,
                    config = foodZoneConfig
                )

                val timeUntilIntake = if (nextIntake != null) {
                    nextIntake.scheduledTime.toEpochMilliseconds() - now.toEpochMilliseconds()
                } else {
                    0L
                }

                _state.value = DashboardState(
                    nextIntake = nextIntake,
                    scheduleConfig = config,
                    foodZoneConfig = foodZoneConfig,
                    currentFoodZone = foodZone,
                    timeUntilIntake = timeUntilIntake,
                    isLoading = false
                )
            }.collect { }
        }
    }

    fun markIntakeTaken() {
        viewModelScope.launch {
            val nextIntake = _state.value.nextIntake ?: return@launch
            val now = Clock.System.now()
            medicationRepository.markIntakeTaken(nextIntake.id, now)

            val hoursUntilNext = _state.value.scheduleConfig.goodHours
            val nextScheduledTime = now.plus(hoursUntilNext, DateTimeUnit.HOUR)

            medicationRepository.insertIntake(
                MedicationIntake(
                    scheduledTime = nextScheduledTime,
                    isCompleted = false
                )
            )
        }
    }

    fun rescheduleNextIntake(newTime: Instant) {
        viewModelScope.launch {
            val nextIntake = _state.value.nextIntake ?: return@launch
            medicationRepository.updateScheduledTime(nextIntake.id, newTime)
        }
    }
}
