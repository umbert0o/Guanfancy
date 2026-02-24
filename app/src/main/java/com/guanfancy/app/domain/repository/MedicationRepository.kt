package com.guanfancy.app.domain.repository

import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.model.FeedbackType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface MedicationRepository {
    fun getAllIntakes(): Flow<List<MedicationIntake>>
    fun getIntakesBetween(start: Instant, end: Instant): Flow<List<MedicationIntake>>
    fun getNextScheduledIntakeFlow(): Flow<MedicationIntake?>
    fun getLastCompletedIntakeFlow(): Flow<MedicationIntake?>
    suspend fun getIntakeById(id: Long): MedicationIntake?
    suspend fun getLatestIntake(): MedicationIntake?
    suspend fun getNextScheduledIntake(): MedicationIntake?
    suspend fun insertIntake(intake: MedicationIntake): Long
    suspend fun updateIntake(intake: MedicationIntake)
    suspend fun markIntakeTaken(intakeId: Long, actualTime: Instant)
    suspend fun updateScheduledTime(intakeId: Long, newScheduledTime: Instant)
    suspend fun submitFeedback(intakeId: Long, feedback: FeedbackType, feedbackTime: Instant)
    suspend fun deleteIntake(intake: MedicationIntake)
    suspend fun deleteAllIntakes()
}
