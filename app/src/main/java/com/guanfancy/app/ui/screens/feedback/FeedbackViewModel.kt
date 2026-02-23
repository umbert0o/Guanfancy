package com.guanfancy.app.ui.screens.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guanfancy.app.domain.model.FeedbackType
import com.guanfancy.app.domain.model.IntakeTimingCalculator
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.model.ScheduleConfig
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import javax.inject.Inject

data class FeedbackState(
    val intake: MedicationIntake? = null,
    val selectedFeedback: FeedbackType? = null,
    val nextScheduledTime: Instant? = null,
    val scheduleConfig: ScheduleConfig = ScheduleConfig.DEFAULT,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackState())
    val state: StateFlow<FeedbackState> = _state.asStateFlow()

    fun loadIntake(intakeId: Long) {
        viewModelScope.launch {
            val config = settingsRepository.scheduleConfig.first()
            val intake = medicationRepository.getIntakeById(intakeId)
            val nextScheduledIntake = medicationRepository.getNextScheduledIntake()

            _state.update {
                it.copy(
                    intake = intake,
                    scheduleConfig = config,
                    nextScheduledTime = nextScheduledIntake?.scheduledTime,
                    isLoading = false
                )
            }
        }
    }

    fun selectFeedback(feedback: FeedbackType) {
        val currentNextTime = _state.value.nextScheduledTime ?: return
        val timeZone = TimeZone.currentSystemDefault()
        
        val delayHours = _state.value.scheduleConfig.getDelayHoursForFeedback(feedback)
        val finalNextTime = IntakeTimingCalculator.applyFeedbackDelay(
            baseTime = currentNextTime,
            delayHours = delayHours,
            timeZone = timeZone
        )

        _state.update {
            it.copy(
                selectedFeedback = feedback,
                nextScheduledTime = finalNextTime
            )
        }
    }

    fun submitFeedback(onComplete: () -> Unit) {
        val intake = _state.value.intake ?: return
        val feedback = _state.value.selectedFeedback ?: return
        val nextTime = _state.value.nextScheduledTime ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            medicationRepository.submitFeedback(
                intakeId = intake.id,
                feedback = feedback,
                feedbackTime = Clock.System.now()
            )

            val nextIntake = medicationRepository.getNextScheduledIntake()
            if (nextIntake != null) {
                medicationRepository.updateScheduledTime(nextIntake.id, nextTime)
            }

            _state.update { it.copy(isSubmitting = false) }
            onComplete()
        }
    }
}
