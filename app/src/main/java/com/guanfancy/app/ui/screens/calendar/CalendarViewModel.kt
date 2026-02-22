package com.guanfancy.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class CalendarState(
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val intakes: List<MedicationIntake> = emptyList(),
    val currentTime: Instant = Clock.System.now()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    init {
        loadIntakesForDate(_state.value.selectedDate)
    }

    fun previousDay() {
        val newDate = _state.value.selectedDate.minus(1, DateTimeUnit.DAY)
        _state.update { it.copy(selectedDate = newDate) }
        loadIntakesForDate(newDate)
    }

    fun nextDay() {
        val newDate = _state.value.selectedDate.plus(1, DateTimeUnit.DAY)
        _state.update { it.copy(selectedDate = newDate) }
        loadIntakesForDate(newDate)
    }

    fun selectDate(date: LocalDate) {
        _state.update { it.copy(selectedDate = date) }
        loadIntakesForDate(date)
    }

    private fun loadIntakesForDate(date: LocalDate) {
        viewModelScope.launch {
            val timeZone = TimeZone.currentSystemDefault()
            val startOfDay = date.atTime(0, 0).toInstant(timeZone)
            val endOfDay = date.atTime(23, 59, 59, 999_999_999).toInstant(timeZone)

            medicationRepository.getIntakesBetween(startOfDay, endOfDay).collect { intakes ->
                _state.update {
                    it.copy(
                        intakes = intakes,
                        currentTime = Clock.System.now()
                    )
                }
            }
        }
    }
}
