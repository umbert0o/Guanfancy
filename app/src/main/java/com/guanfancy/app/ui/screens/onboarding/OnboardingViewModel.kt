package com.guanfancy.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.model.MedicationType
import com.guanfancy.app.domain.model.ScheduleConfig
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class OnboardingState(
    val step: Int = 0,
    val medicationType: MedicationType = MedicationType.DEFAULT,
    val selectedHour: Int = ScheduleConfig.DEFAULT.defaultIntakeTimeHour,
    val selectedMinute: Int = ScheduleConfig.DEFAULT.defaultIntakeTimeMinute,
    val scheduleConfig: ScheduleConfig = ScheduleConfig.DEFAULT,
    val isLoading: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setMedicationType(type: MedicationType) {
        _state.value = _state.value.copy(medicationType = type)
    }

    fun setHour(hour: Int) {
        _state.value = _state.value.copy(selectedHour = hour)
    }

    fun setMinute(minute: Int) {
        _state.value = _state.value.copy(selectedMinute = minute)
    }

    fun setGoodHours(hours: Int) {
        _state.value = _state.value.copy(
            scheduleConfig = _state.value.scheduleConfig.copy(goodHours = hours)
        )
    }

    fun setDizzyHours(hours: Int) {
        _state.value = _state.value.copy(
            scheduleConfig = _state.value.scheduleConfig.copy(dizzyHours = hours)
        )
    }

    fun setTooDizzyHours(hours: Int) {
        _state.value = _state.value.copy(
            scheduleConfig = _state.value.scheduleConfig.copy(tooDizzyHours = hours)
        )
    }

    fun setFeedbackDelayHours(hours: Int) {
        _state.value = _state.value.copy(
            scheduleConfig = _state.value.scheduleConfig.copy(feedbackDelayHours = hours)
        )
    }

    fun nextStep() {
        val currentStep = _state.value.step
        if (currentStep < TOTAL_STEPS - 1) {
            _state.value = _state.value.copy(step = currentStep + 1)
        }
    }

    fun previousStep() {
        val currentStep = _state.value.step
        if (currentStep > 0) {
            _state.value = _state.value.copy(step = currentStep - 1)
        }
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(timeZone).date
            val selectedTime = LocalTime(_state.value.selectedHour, _state.value.selectedMinute)

            var scheduledInstant = selectedTime.atDate(today).toInstant(timeZone)
            if (scheduledInstant <= now) {
                scheduledInstant = scheduledInstant.plus(1, DateTimeUnit.DAY, timeZone)
            }

            settingsRepository.setMedicationType(_state.value.medicationType)
            settingsRepository.setCurrentIntakeTime(scheduledInstant)
            settingsRepository.setWarningAccepted(true)
            settingsRepository.setOnboardingCompleted(true)

            val config = _state.value.scheduleConfig.copy(
                defaultIntakeTimeHour = _state.value.selectedHour,
                defaultIntakeTimeMinute = _state.value.selectedMinute
            )
            settingsRepository.updateScheduleConfig(config)

            val intake = MedicationIntake(
                scheduledTime = scheduledInstant,
                isCompleted = false
            )
            medicationRepository.insertIntake(intake)

            _state.value = _state.value.copy(isLoading = false)
            onComplete()
        }
    }

    companion object {
        const val TOTAL_STEPS = 5
    }
}
