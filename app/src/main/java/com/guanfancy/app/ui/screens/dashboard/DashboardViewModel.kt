package com.guanfancy.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guanfancy.app.domain.model.FoodZone
import com.guanfancy.app.domain.model.FoodZoneCalculator
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.IntakeTimingCalculator
import com.guanfancy.app.domain.model.IntakeTimingResult
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.model.ScheduleConfig
import com.guanfancy.app.data.notifications.NotificationHelper
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class DashboardState(
    val nextIntake: MedicationIntake? = null,
    val scheduleConfig: ScheduleConfig = ScheduleConfig.DEFAULT,
    val foodZoneConfig: FoodZoneConfig = FoodZoneConfig.DEFAULT,
    val currentFoodZone: FoodZone = FoodZone.GREEN,
    val timeUntilIntake: Long = 0,
    val isLoading: Boolean = true,
    val showRescheduleDialog: Boolean = false,
    val pendingIntakeId: Long? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
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
                    isLoading = false,
                    showRescheduleDialog = _state.value.showRescheduleDialog,
                    pendingIntakeId = _state.value.pendingIntakeId
                )
            }.collect { }
        }
    }

    fun markIntakeTaken(onShowReschedulePrompt: () -> Unit = {}) {
        viewModelScope.launch {
            val nextIntake = _state.value.nextIntake ?: return@launch
            val now = Clock.System.now()
            val config = _state.value.scheduleConfig
            val timeZone = TimeZone.currentSystemDefault()
            
            medicationRepository.markIntakeTaken(nextIntake.id, now)

            val timingResult = IntakeTimingCalculator.calculateForScheduledIntake(
                now = now,
                scheduledTime = nextIntake.scheduledTime,
                timeZone = timeZone
            )

            val nextIntakeTime = IntakeTimingCalculator.calculateNextIntakeAfterTaking(
                now = now,
                defaultHour = config.defaultIntakeTimeHour,
                defaultMinute = config.defaultIntakeTimeMinute,
                timeZone = timeZone
            )
            
            medicationRepository.insertIntake(
                MedicationIntake(
                    scheduledTime = nextIntakeTime,
                    isCompleted = false
                )
            )

            if (timingResult.needsReschedulePrompt) {
                _state.update { 
                    it.copy(
                        showRescheduleDialog = true,
                        pendingIntakeId = nextIntake.id
                    )
                }
                onShowReschedulePrompt()
            } else {
                notificationHelper.scheduleFeedbackReminder(nextIntake.id, config.feedbackDelayHours)
            }
        }
    }

    fun confirmRescheduleDefaultTime() {
        viewModelScope.launch {
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val localTime = now.toLocalDateTime(timeZone).time
            
            val newConfig = _state.value.scheduleConfig.copy(
                defaultIntakeTimeHour = localTime.hour,
                defaultIntakeTimeMinute = localTime.minute
            )
            settingsRepository.updateScheduleConfig(newConfig)
            
            val nextIntake = medicationRepository.getNextScheduledIntake()
            if (nextIntake != null) {
                val newNextIntakeTime = IntakeTimingCalculator.calculateNextIntakeAfterTaking(
                    now = now,
                    defaultHour = localTime.hour,
                    defaultMinute = localTime.minute,
                    timeZone = timeZone
                )
                medicationRepository.updateScheduledTime(nextIntake.id, newNextIntakeTime)
            }
            
            val pendingId = _state.value.pendingIntakeId
            if (pendingId != null) {
                notificationHelper.scheduleFeedbackReminder(pendingId, newConfig.feedbackDelayHours)
            }
            
            _state.update { 
                it.copy(
                    showRescheduleDialog = false,
                    pendingIntakeId = null
                )
            }
        }
    }

    fun declineRescheduleDefaultTime() {
        viewModelScope.launch {
            val pendingId = _state.value.pendingIntakeId
            val config = _state.value.scheduleConfig
            if (pendingId != null) {
                notificationHelper.scheduleFeedbackReminder(pendingId, config.feedbackDelayHours)
            }
            
            _state.update { 
                it.copy(
                    showRescheduleDialog = false,
                    pendingIntakeId = null
                )
            }
        }
    }

    fun dismissRescheduleDialog() {
        _state.update { 
            it.copy(
                showRescheduleDialog = false,
                pendingIntakeId = null
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
