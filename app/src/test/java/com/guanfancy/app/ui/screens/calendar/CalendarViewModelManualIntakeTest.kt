package com.guanfancy.app.ui.screens.calendar

import com.guanfancy.app.domain.model.FeedbackType
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.IntakeSource
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.model.ScheduleConfig
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelManualIntakeTest {

    private lateinit var viewModel: CalendarViewModel
    private lateinit var medicationRepository: TestMedicationRepository
    private lateinit var settingsRepository: TestSettingsRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        medicationRepository = TestMedicationRepository()
        settingsRepository = TestSettingsRepository()
        viewModel = CalendarViewModel(medicationRepository, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun showAddManualIntakeDialog_updatesState() = runTest {
        viewModel.showAddManualIntakeDialog(hour = 14)

        val state = viewModel.state.first()
        assertTrue(state.showAddDialog)
        assertEquals(14, state.selectedHour)
    }

    @Test
    fun hideAddDialog_clearsState() = runTest {
        viewModel.showAddManualIntakeDialog(hour = 14)
        viewModel.hideAddDialog()

        val state = viewModel.state.first()
        assertFalse(state.showAddDialog)
        assertNull(state.selectedHour)
    }

    @Test
    fun addManualIntake_createsIntakeWithCorrectSource() = runTest {
        viewModel.showAddManualIntakeDialog(hour = 14)
        viewModel.addManualIntake()
        testDispatcher.scheduler.advanceUntilIdle()

        val intakes = medicationRepository.getAllIntakes().first()
        assertEquals(1, intakes.size)
        assertEquals(IntakeSource.MANUAL, intakes.first().source)
    }

    @Test
    fun addManualIntake_createsCompletedIntake() = runTest {
        viewModel.showAddManualIntakeDialog(hour = 14)
        viewModel.addManualIntake()
        testDispatcher.scheduler.advanceUntilIdle()

        val intakes = medicationRepository.getAllIntakes().first()
        assertTrue(intakes.first().isCompleted)
        assertNotNull(intakes.first().actualTime)
    }

    @Test
    fun addManualIntake_hidesDialog() = runTest {
        viewModel.showAddManualIntakeDialog(hour = 14)
        viewModel.addManualIntake()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertFalse(state.showAddDialog)
    }

    @Test
    fun addManualIntake_usesSelectedHour() = runTest {
        viewModel.showAddManualIntakeDialog(hour = 16)
        viewModel.addManualIntake()
        testDispatcher.scheduler.advanceUntilIdle()

        val intakes = medicationRepository.getAllIntakes().first()
        assertEquals(1, intakes.size)
        val localTime = intakes.first().actualTime!!.toLocalDateTime(TimeZone.currentSystemDefault()).time
        assertEquals(16, localTime.hour)
        assertEquals(0, localTime.minute)
    }

    @Test
    fun showEditManualIntakeDialog_updatesStateForManualIntake() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        viewModel.showEditManualIntakeDialog(manualIntake)

        val state = viewModel.state.first()
        assertTrue(state.showEditDialog)
        assertEquals(manualIntake, state.selectedIntake)
    }

    @Test
    fun showEditManualIntakeDialog_ignoresScheduledIntake() = runTest {
        val now = Clock.System.now()
        val scheduledIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.SCHEDULED
        )

        viewModel.showEditManualIntakeDialog(scheduledIntake)

        val state = viewModel.state.first()
        assertFalse(state.showEditDialog)
    }

    @Test
    fun hideEditDialog_clearsState() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        viewModel.showEditManualIntakeDialog(manualIntake)
        viewModel.hideEditDialog()

        val state = viewModel.state.first()
        assertFalse(state.showEditDialog)
        assertNull(state.selectedIntake)
    }

    @Test
    fun updateManualIntake_updatesIntakeTime() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )
        medicationRepository.insertIntake(manualIntake)

        viewModel.showEditManualIntakeDialog(manualIntake)
        viewModel.updateManualIntake(LocalTime(16, 45))
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = medicationRepository.getIntakeById(1L)
        assertNotNull(updated)
        val localTime = updated!!.actualTime!!.toLocalDateTime(TimeZone.currentSystemDefault()).time
        assertEquals(16, localTime.hour)
        assertEquals(45, localTime.minute)
    }

    @Test
    fun updateManualIntake_hidesDialog() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )
        medicationRepository.insertIntake(manualIntake)

        viewModel.showEditManualIntakeDialog(manualIntake)
        viewModel.updateManualIntake(LocalTime(16, 45))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertFalse(state.showEditDialog)
    }

    @Test
    fun showDeleteConfirmDialog_showsDialog() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )

        viewModel.showEditManualIntakeDialog(manualIntake)
        viewModel.showDeleteConfirmDialog()

        val state = viewModel.state.first()
        assertTrue(state.showDeleteConfirmDialog)
        assertFalse(state.showEditDialog)
    }

    @Test
    fun hideDeleteConfirmDialog_clearsState() = runTest {
        viewModel.showDeleteConfirmDialog()
        viewModel.hideDeleteConfirmDialog()

        val state = viewModel.state.first()
        assertFalse(state.showDeleteConfirmDialog)
    }

    @Test
    fun deleteManualIntake_removesIntake() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )
        medicationRepository.insertIntake(manualIntake)

        viewModel.showEditManualIntakeDialog(manualIntake)
        viewModel.showDeleteConfirmDialog()
        viewModel.deleteManualIntake()
        testDispatcher.scheduler.advanceUntilIdle()

        val deleted = medicationRepository.getIntakeById(1L)
        assertNull(deleted)
    }

    @Test
    fun deleteManualIntake_hidesDialog() = runTest {
        val now = Clock.System.now()
        val manualIntake = MedicationIntake(
            id = 1L,
            scheduledTime = now,
            actualTime = now,
            isCompleted = true,
            source = IntakeSource.MANUAL
        )
        medicationRepository.insertIntake(manualIntake)

        viewModel.showEditManualIntakeDialog(manualIntake)
        viewModel.showDeleteConfirmDialog()
        viewModel.deleteManualIntake()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertFalse(state.showDeleteConfirmDialog)
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
            val id = if (intake.id == 0L) nextId++ else intake.id
            if (intake.id == 0L) {
                intakes.add(intake.copy(id = id))
            } else {
                intakes.add(intake)
            }
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

    private class TestSettingsRepository : SettingsRepository {
        override val foodZoneConfig = flowOf(FoodZoneConfig.DEFAULT)
        override val isWarningAccepted = flowOf(true)
        override val isOnboardingCompleted = flowOf(true)
        override val medicationType = flowOf(com.guanfancy.app.domain.model.MedicationType.INTUNIV)
        override val scheduleConfig = flowOf(ScheduleConfig.DEFAULT)
        override val currentIntakeTime = flowOf<Instant?>(null)

        override suspend fun setWarningAccepted(accepted: Boolean) {}
        override suspend fun setOnboardingCompleted(completed: Boolean) {}
        override suspend fun setMedicationType(type: com.guanfancy.app.domain.model.MedicationType) {}
        override suspend fun updateScheduleConfig(config: ScheduleConfig) {}
        override suspend fun setCurrentIntakeTime(time: Instant) {}
        override suspend fun clearAllData() {}
    }
}
