package com.guanfancy.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.guanfancy.app.domain.model.MedicationType
import com.guanfancy.app.domain.model.ScheduleConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "guanfancy_settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val WARNING_ACCEPTED = booleanPreferencesKey("warning_accepted")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val MEDICATION_TYPE = stringPreferencesKey("medication_type")
        val GOOD_HOURS = intPreferencesKey("good_hours")
        val DIZZY_HOURS = intPreferencesKey("dizzy_hours")
        val TOO_DIZZY_HOURS = intPreferencesKey("too_dizzy_hours")
        val FEEDBACK_DELAY_HOURS = intPreferencesKey("feedback_delay_hours")
        val DEFAULT_INTAKE_HOUR = intPreferencesKey("default_intake_hour")
        val DEFAULT_INTAKE_MINUTE = intPreferencesKey("default_intake_minute")
        val CURRENT_INTAKE_TIME = longPreferencesKey("current_intake_time")
    }

    val isWarningAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.WARNING_ACCEPTED] ?: false }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETED] ?: false }

    val medicationType: Flow<MedicationType> = context.dataStore.data
        .map { preferences ->
            MedicationType.fromString(preferences[Keys.MEDICATION_TYPE])
        }

    val scheduleConfig: Flow<ScheduleConfig> = context.dataStore.data
        .map { preferences ->
            ScheduleConfig(
                goodHours = preferences[Keys.GOOD_HOURS] ?: ScheduleConfig.DEFAULT.goodHours,
                dizzyHours = preferences[Keys.DIZZY_HOURS] ?: ScheduleConfig.DEFAULT.dizzyHours,
                tooDizzyHours = preferences[Keys.TOO_DIZZY_HOURS] ?: ScheduleConfig.DEFAULT.tooDizzyHours,
                feedbackDelayHours = preferences[Keys.FEEDBACK_DELAY_HOURS] ?: ScheduleConfig.DEFAULT.feedbackDelayHours,
                defaultIntakeTimeHour = preferences[Keys.DEFAULT_INTAKE_HOUR] ?: ScheduleConfig.DEFAULT.defaultIntakeTimeHour,
                defaultIntakeTimeMinute = preferences[Keys.DEFAULT_INTAKE_MINUTE] ?: ScheduleConfig.DEFAULT.defaultIntakeTimeMinute
            )
        }

    val currentIntakeTime: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[Keys.CURRENT_INTAKE_TIME] }

    suspend fun setWarningAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.WARNING_ACCEPTED] = accepted
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setMedicationType(type: MedicationType) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MEDICATION_TYPE] = type.name
        }
    }

    suspend fun updateScheduleConfig(config: ScheduleConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.GOOD_HOURS] = config.goodHours
            preferences[Keys.DIZZY_HOURS] = config.dizzyHours
            preferences[Keys.TOO_DIZZY_HOURS] = config.tooDizzyHours
            preferences[Keys.FEEDBACK_DELAY_HOURS] = config.feedbackDelayHours
            preferences[Keys.DEFAULT_INTAKE_HOUR] = config.defaultIntakeTimeHour
            preferences[Keys.DEFAULT_INTAKE_MINUTE] = config.defaultIntakeTimeMinute
        }
    }

    suspend fun setCurrentIntakeTime(epochMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CURRENT_INTAKE_TIME] = epochMillis
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
