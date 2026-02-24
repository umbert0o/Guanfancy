package com.guanfancy.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.guanfancy.app.data.local.entity.IntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {
    @Query("SELECT * FROM medication_intakes ORDER BY scheduledTimeEpoch DESC")
    fun getAllIntakes(): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM medication_intakes WHERE scheduledTimeEpoch >= :startEpoch AND scheduledTimeEpoch <= :endEpoch ORDER BY scheduledTimeEpoch ASC")
    fun getIntakesBetween(startEpoch: Long, endEpoch: Long): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM medication_intakes WHERE id = :id")
    suspend fun getIntakeById(id: Long): IntakeEntity?

    @Query("SELECT * FROM medication_intakes ORDER BY scheduledTimeEpoch DESC LIMIT 1")
    suspend fun getLatestIntake(): IntakeEntity?

    @Query("SELECT * FROM medication_intakes WHERE isCompleted = 0 ORDER BY scheduledTimeEpoch ASC LIMIT 1")
    suspend fun getNextScheduledIntake(): IntakeEntity?

    @Query("SELECT * FROM medication_intakes WHERE isCompleted = 0 ORDER BY scheduledTimeEpoch ASC LIMIT 1")
    fun getNextScheduledIntakeFlow(): Flow<IntakeEntity?>

    @Query("SELECT * FROM medication_intakes WHERE isCompleted = 1 AND actualTimeEpoch IS NOT NULL AND source = 'SCHEDULED' ORDER BY actualTimeEpoch DESC LIMIT 1")
    fun getLastCompletedIntakeFlow(): Flow<IntakeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: IntakeEntity): Long

    @Update
    suspend fun updateIntake(intake: IntakeEntity)

    @Query("UPDATE medication_intakes SET actualTimeEpoch = :actualTimeEpoch, isCompleted = 1 WHERE id = :id")
    suspend fun markIntakeTaken(id: Long, actualTimeEpoch: Long)

    @Query("UPDATE medication_intakes SET feedbackType = :feedbackType, feedbackTimeEpoch = :feedbackTimeEpoch, nextScheduledTimeEpoch = :nextScheduledTimeEpoch WHERE id = :id")
    suspend fun submitFeedback(id: Long, feedbackType: String, feedbackTimeEpoch: Long, nextScheduledTimeEpoch: Long?)

    @Query("DELETE FROM medication_intakes WHERE id = :id")
    suspend fun deleteIntake(id: Long)

    @Query("DELETE FROM medication_intakes")
    suspend fun deleteAll()
}
