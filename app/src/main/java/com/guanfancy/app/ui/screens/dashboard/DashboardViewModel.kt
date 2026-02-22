package com.guanfancy.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guanfancy.app.domain.model.FoodZone
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.model.ScheduleConfig
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
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
                val now = Clock.System.now()

                val foodZone = calculateFoodZone(now, nextIntake?.scheduledTime, foodZoneConfig)

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

    private fun calculateFoodZone(now: Instant, intakeTime: Instant?, config: FoodZoneConfig): FoodZone {
        if (intakeTime == null) return FoodZone.GREEN

        val diffMillis = intakeTime.toEpochMilliseconds() - now.toEpochMilliseconds()
        val diffHours = diffMillis / (1000 * 60 * 60.0)

        return when {
            diffHours < config.yellowHoursBefore -> FoodZone.RED
            diffHours < config.greenHoursBefore -> FoodZone.YELLOW
            else -> FoodZone.GREEN
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
}
