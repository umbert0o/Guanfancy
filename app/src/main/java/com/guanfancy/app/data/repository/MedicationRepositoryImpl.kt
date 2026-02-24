package com.guanfancy.app.data.repository

import com.guanfancy.app.data.local.dao.IntakeDao
import com.guanfancy.app.data.local.entity.toDomain
import com.guanfancy.app.data.local.entity.toEntity
import com.guanfancy.app.domain.model.FeedbackType
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val intakeDao: IntakeDao
) : MedicationRepository {

    override fun getAllIntakes(): Flow<List<MedicationIntake>> {
        return intakeDao.getAllIntakes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getIntakesBetween(start: Instant, end: Instant): Flow<List<MedicationIntake>> {
        return intakeDao.getIntakesBetween(
            startEpoch = start.toEpochMilliseconds(),
            endEpoch = end.toEpochMilliseconds()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNextScheduledIntakeFlow(): Flow<MedicationIntake?> {
        return intakeDao.getNextScheduledIntakeFlow().map { it?.toDomain() }
    }

    override fun getLastCompletedIntakeFlow(): Flow<MedicationIntake?> {
        return intakeDao.getLastCompletedIntakeFlow().map { it?.toDomain() }
    }

    override suspend fun getIntakeById(id: Long): MedicationIntake? {
        return intakeDao.getIntakeById(id)?.toDomain()
    }

    override suspend fun getLatestIntake(): MedicationIntake? {
        return intakeDao.getLatestIntake()?.toDomain()
    }

    override suspend fun getNextScheduledIntake(): MedicationIntake? {
        return intakeDao.getNextScheduledIntake()?.toDomain()
    }

    override suspend fun insertIntake(intake: MedicationIntake): Long {
        return intakeDao.insertIntake(intake.toEntity())
    }

    override suspend fun updateIntake(intake: MedicationIntake) {
        intakeDao.updateIntake(intake.toEntity())
    }

    override suspend fun markIntakeTaken(intakeId: Long, actualTime: Instant) {
        intakeDao.markIntakeTaken(
            id = intakeId,
            actualTimeEpoch = actualTime.toEpochMilliseconds()
        )
    }

    override suspend fun updateScheduledTime(intakeId: Long, newScheduledTime: Instant) {
        val intake = intakeDao.getIntakeById(intakeId) ?: return
        val updatedIntake = intake.copy(scheduledTimeEpoch = newScheduledTime.toEpochMilliseconds())
        intakeDao.updateIntake(updatedIntake)
    }

    override suspend fun submitFeedback(intakeId: Long, feedback: FeedbackType, feedbackTime: Instant) {
        intakeDao.submitFeedback(
            id = intakeId,
            feedbackType = feedback.name,
            feedbackTimeEpoch = feedbackTime.toEpochMilliseconds(),
            nextScheduledTimeEpoch = null
        )
    }

    override suspend fun deleteIntake(intake: MedicationIntake) {
        intakeDao.deleteIntake(intake.id)
    }

    override suspend fun deleteAllIntakes() {
        intakeDao.deleteAll()
    }
}
