package com.guanfancy.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.MedicationType
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
import javax.inject.Inject

data class SettingsState(
    val medicationType: MedicationType = MedicationType.DEFAULT,
    val foodZoneConfig: FoodZoneConfig = FoodZoneConfig.DEFAULT,
    val scheduleConfig: ScheduleConfig = ScheduleConfig.DEFAULT,
    val isLoading: Boolean = true,
    val showResetDialog: Boolean = false,
    val isResetting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val config = settingsRepository.scheduleConfig.first()
            val medType = settingsRepository.medicationType.first()
            val foodConfig = settingsRepository.foodZoneConfig.first()
            _state.update {
                it.copy(
                    medicationType = medType,
                    foodZoneConfig = foodConfig,
                    scheduleConfig = config,
                    isLoading = false
                )
            }
        }
    }

    fun updateMedicationType(type: MedicationType) {
        _state.update {
            it.copy(
                medicationType = type,
                foodZoneConfig = type.getFoodZoneConfig()
            )
        }
        viewModelScope.launch {
            settingsRepository.setMedicationType(type)
        }
    }

    fun updateGoodHours(hours: Int) {
        _state.update { it.copy(scheduleConfig = it.scheduleConfig.copy(goodHours = hours)) }
        saveConfig()
    }

    fun updateDizzyHours(hours: Int) {
        _state.update { it.copy(scheduleConfig = it.scheduleConfig.copy(dizzyHours = hours)) }
        saveConfig()
    }

    fun updateTooDizzyHours(hours: Int) {
        _state.update { it.copy(scheduleConfig = it.scheduleConfig.copy(tooDizzyHours = hours)) }
        saveConfig()
    }

    fun updateFeedbackDelayHours(hours: Int) {
        _state.update { it.copy(scheduleConfig = it.scheduleConfig.copy(feedbackDelayHours = hours)) }
        saveConfig()
    }

    private fun saveConfig() {
        viewModelScope.launch {
            settingsRepository.updateScheduleConfig(_state.value.scheduleConfig)
        }
    }

    fun showResetDialog() {
        _state.update { it.copy(showResetDialog = true) }
    }

    fun hideResetDialog() {
        _state.update { it.copy(showResetDialog = false) }
    }

    fun resetApp(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isResetting = true) }
            
            medicationRepository.deleteAllIntakes()
            settingsRepository.clearAllData()
            
            _state.update { it.copy(isResetting = false, showResetDialog = false) }
            onComplete()
        }
    }
}
