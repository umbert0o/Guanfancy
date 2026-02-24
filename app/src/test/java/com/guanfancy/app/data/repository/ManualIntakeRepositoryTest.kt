package com.guanfancy.app.data.repository

import com.guanfancy.app.domain.model.FeedbackType
import com.guanfancy.app.domain.model.IntakeSource
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ManualIntakeRepositoryTest {

    private lateinit var repository: TestMedicationRepository

    @Before
    fun setup() {
        repository = TestMedicationRepository()
    }

    @Test
    fun manualIntake_isStoredWithCorrectSource() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        repository.insertIntake(manualIntake)

        val stored = repository.getIntakeById(1L)
        assertNotNull(stored)
        assertEquals(IntakeSource.MANUAL, stored?.source)
    }

    @Test
    fun manualIntake_isAlwaysCompleted() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        repository.insertIntake(manualIntake)

        val stored = repository.getIntakeById(1L)
        assertTrue(stored?.isCompleted == true)
        assertNotNull(stored?.actualTime)
    }

    @Test
    fun manualIntake_doesNotAppearInNextScheduledIntake() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        repository.insertIntake(manualIntake)

        val nextScheduled = repository.getNextScheduledIntake()
        assertNull(nextScheduled)
    }

    @Test
    fun manualIntake_doesNotAppearInLastCompletedIntake() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        repository.insertIntake(manualIntake)

        val lastCompleted = repository.getLastCompletedIntakeFlow().first()
        assertNull(lastCompleted)
    }

    @Test
    fun scheduledIntake_appearsInLastCompletedIntake() = runTest {
        val now = Clock.System.now()
        val scheduledIntake = MedicationIntake(
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.SCHEDULED
        )

        repository.insertIntake(scheduledIntake)

        val lastCompleted = repository.getLastCompletedIntakeFlow().first()
        assertNotNull(lastCompleted)
        assertEquals(IntakeSource.SCHEDULED, lastCompleted?.source)
    }

    @Test
    fun manualIntake_appearsInDateRangeQuery() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        repository.insertIntake(manualIntake)

        val start = now - 1.hours
        val end = now + 1.hours
        val intakesInRange = repository.getIntakesBetween(start, end).first()

        assertEquals(1, intakesInRange.size)
        assertEquals(IntakeSource.MANUAL, intakesInRange.first().source)
    }

    @Test
    fun updateManualIntake_preservesSource() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        repository.insertIntake(manualIntake)

        val updatedIntake = manualIntake.copy(
            scheduledTime = now + 30.minutes,
            actualTime = now + 30.minutes
        )
        repository.updateIntake(updatedIntake)

        val stored = repository.getIntakeById(1L)
        assertNotNull(stored)
        assertEquals(IntakeSource.MANUAL, stored?.source)
        assertEquals(now + 30.minutes, stored?.actualTime)
    }

    @Test
    fun deleteManualIntake_removesFromRepository() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        repository.insertIntake(manualIntake)
        repository.deleteIntake(manualIntake)

        val stored = repository.getIntakeById(1L)
        assertNull(stored)
    }

    @Test
    fun mixedIntakes_onlyScheduledAffectsLastCompleted() = runTest {
        val now = Clock.System.now()
        
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now - 2.hours,
            actualTime = now - 2.hours,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )
        val scheduledIntake = MedicationIntake(
            id = 2L,
            scheduledTime = now - 1.hours,
            actualTime = now - 1.hours,
            isCompleted = true,
            source = IntakeSource.SCHEDULED
        )

        repository.insertIntake(manualIntake)
        repository.insertIntake(scheduledIntake)

        val lastCompleted = repository.getLastCompletedIntakeFlow().first()
        assertNotNull(lastCompleted)
        assertEquals(IntakeSource.SCHEDULED, lastCompleted?.source)
        assertEquals(2L, lastCompleted?.id)
    }

    private class TestMedicationRepository : MedicationRepository {
        private val intakes = mutableListOf<MedicationIntake>()
        private var nextId = 1L

        override fun getAllIntakes() = flowOf(intakes.toList())

        override fun getIntakesBetween(start: Instant, end: Instant) = flowOf(
            intakes.filter { it.scheduledTime >= start && it.scheduledTime <= end }
        )

        override fun getNextScheduledIntakeFlow() = flowOf(
            intakes.filter { !it.isCompleted }.minByOrNull { it.scheduledTime }
        )

        override fun getLastCompletedIntakeFlow() = flowOf(
            intakes
                .filter { it.isCompleted && it.actualTime != null && it.source == IntakeSource.SCHEDULED }
                .maxByOrNull { it.actualTime!! }
        )

        override suspend fun getIntakeById(id: Long) = intakes.find { it.id == id }

        override suspend fun getLatestIntake() = intakes.maxByOrNull { it.scheduledTime }

        override suspend fun getNextScheduledIntake() = intakes
            .filter { !it.isCompleted }
            .minByOrNull { it.scheduledTime }

        override suspend fun insertIntake(intake: MedicationIntake): Long {
            val id = nextId++
            intakes.add(intake.copy(id = id))
            return id
        }

        override suspend fun updateIntake(intake: MedicationIntake) {
            val index = intakes.indexOfFirst { it.id == intake.id }
            if (index >= 0) {
                intakes[index] = intake
            }
        }

        override suspend fun markIntakeTaken(intakeId: Long, actualTime: Instant) {
            val index = intakes.indexOfFirst { it.id == intakeId }
            if (index >= 0) {
                intakes[index] = intakes[index].copy(
                    actualTime = actualTime,
                    isCompleted = true
                )
            }
        }

        override suspend fun updateScheduledTime(intakeId: Long, newScheduledTime: Instant) {
            val index = intakes.indexOfFirst { it.id == intakeId }
            if (index >= 0) {
                intakes[index] = intakes[index].copy(scheduledTime = newScheduledTime)
            }
        }

        override suspend fun submitFeedback(intakeId: Long, feedback: FeedbackType, feedbackTime: Instant) {
            val index = intakes.indexOfFirst { it.id == intakeId }
            if (index >= 0) {
                intakes[index] = intakes[index].copy(
                    feedback = feedback,
                    feedbackTime = feedbackTime
                )
            }
        }

        override suspend fun deleteIntake(intake: MedicationIntake) {
            intakes.removeAll { it.id == intake.id }
        }

        override suspend fun deleteAllIntakes() {
            intakes.clear()
        }
    }
}
