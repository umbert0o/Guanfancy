package com.guanfancy.app.domain.service

import com.guanfancy.app.domain.model.FoodZone
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.domain.repository.MedicationRepository
import com.guanfancy.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class FoodZoneServiceTest {

    private lateinit var service: FoodZoneServiceImpl
    private lateinit var mockMedicationRepository: MockMedicationRepository
    private lateinit var mockSettingsRepository: MockSettingsRepository

    private val defaultConfig = FoodZoneConfig(
        greenHoursBefore = 5,
        yellowHoursBefore = 3,
        redHoursAfter = 3,
        yellowHoursAfter = 5
    )

    @Before
    fun setup() {
        mockMedicationRepository = MockMedicationRepository()
        mockSettingsRepository = MockSettingsRepository(defaultConfig)
        service = FoodZoneServiceImpl(mockMedicationRepository, mockSettingsRepository)
    }

    @Test
    fun whenNoCompletedIntakeAndNoScheduledIntake_returnsGreen() = runTest {
        mockMedicationRepository.lastCompletedIntake = null
        mockMedicationRepository.nextScheduledIntake = null

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.GREEN, result)
    }

    @Test
    fun whenWithinRedHoursAfterIntake_returnsRed() = runTest {
        val now = Clock.System.now()
        val intakeTime = now - 2.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(intakeTime)
        mockMedicationRepository.nextScheduledIntake = null

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.RED, result)
    }

    @Test
    fun whenWithinYellowHoursAfterIntake_returnsYellow() = runTest {
        val now = Clock.System.now()
        val intakeTime = now - 4.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(intakeTime)
        mockMedicationRepository.nextScheduledIntake = null

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.YELLOW, result)
    }

    @Test
    fun whenAfterYellowHoursAfterIntake_returnsGreen() = runTest {
        val now = Clock.System.now()
        val intakeTime = now - 6.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(intakeTime)
        mockMedicationRepository.nextScheduledIntake = null

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.GREEN, result)
    }

    @Test
    fun whenWithinRedHoursBeforeScheduledIntake_returnsRed() = runTest {
        val now = Clock.System.now()
        val scheduledTime = now + 2.hours
        mockMedicationRepository.lastCompletedIntake = null
        mockMedicationRepository.nextScheduledIntake = createScheduledIntake(scheduledTime)

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.RED, result)
    }

    @Test
    fun whenWithinYellowHoursBeforeScheduledIntake_returnsYellow() = runTest {
        val now = Clock.System.now()
        val scheduledTime = now + 4.hours
        mockMedicationRepository.lastCompletedIntake = null
        mockMedicationRepository.nextScheduledIntake = createScheduledIntake(scheduledTime)

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.YELLOW, result)
    }

    @Test
    fun whenBeforeGreenHoursBeforeScheduledIntake_returnsGreen() = runTest {
        val now = Clock.System.now()
        val scheduledTime = now + 6.hours
        mockMedicationRepository.lastCompletedIntake = null
        mockMedicationRepository.nextScheduledIntake = createScheduledIntake(scheduledTime)

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.GREEN, result)
    }

    @Test
    fun whenBothAfterAndBeforeZonesApply_returnsMostRestrictive() = runTest {
        val now = Clock.System.now()
        val lastIntakeTime = now - 4.hours
        val nextScheduledTime = now + 4.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(lastIntakeTime)
        mockMedicationRepository.nextScheduledIntake = createScheduledIntake(nextScheduledTime)

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.YELLOW, result)
    }

    @Test
    fun whenAfterZoneIsYellowAndBeforeZoneIsRed_returnsRed() = runTest {
        val now = Clock.System.now()
        val lastIntakeTime = now - 4.hours
        val nextScheduledTime = now + 2.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(lastIntakeTime)
        mockMedicationRepository.nextScheduledIntake = createScheduledIntake(nextScheduledTime)

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.RED, result)
    }

    @Test
    fun whenAfterZoneIsRedAndBeforeZoneIsYellow_returnsRed() = runTest {
        val now = Clock.System.now()
        val lastIntakeTime = now - 2.hours
        val nextScheduledTime = now + 4.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(lastIntakeTime)
        mockMedicationRepository.nextScheduledIntake = createScheduledIntake(nextScheduledTime)

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.RED, result)
    }

    @Test
    fun whenExactlyAtIntakeTime_returnsRed() = runTest {
        val now = Clock.System.now()
        mockMedicationRepository.lastCompletedIntake = null
        mockMedicationRepository.nextScheduledIntake = createScheduledIntake(now)

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.RED, result)
    }

    @Test
    fun whenExactlyAtEndOfRedHoursAfter_returnsYellow() = runTest {
        val now = Clock.System.now()
        val intakeTime = now - 3.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(intakeTime)
        mockMedicationRepository.nextScheduledIntake = null

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.YELLOW, result)
    }

    @Test
    fun whenExactlyAtEndOfYellowHoursAfter_returnsGreen() = runTest {
        val now = Clock.System.now()
        val intakeTime = now - 5.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(intakeTime)
        mockMedicationRepository.nextScheduledIntake = null

        val result = service.getCurrentZone().first()

        assertEquals(FoodZone.GREEN, result)
    }

    @Test
    fun whenConfigChanges_reflectedInZoneCalculation() = runTest {
        val now = Clock.System.now()
        val intakeTime = now - 2.hours
        mockMedicationRepository.lastCompletedIntake = createCompletedIntake(intakeTime)
        mockMedicationRepository.nextScheduledIntake = null

        val resultWithDefaultConfig = service.getCurrentZone().first()
        assertEquals(FoodZone.RED, resultWithDefaultConfig)

        val shorterConfig = FoodZoneConfig(
            greenHoursBefore = 5,
            yellowHoursBefore = 3,
            redHoursAfter = 1,
            yellowHoursAfter = 3
        )
        mockSettingsRepository.config = shorterConfig

        val resultWithShorterConfig = service.getCurrentZone().first()
        assertEquals(FoodZone.YELLOW, resultWithShorterConfig)
    }

    private fun createCompletedIntake(actualTime: Instant): MedicationIntake {
        return MedicationIntake(
            id = 1L,
            scheduledTime = actualTime,
            actualTime = actualTime,
            isCompleted = true
        )
    }

    private fun createScheduledIntake(scheduledTime: Instant): MedicationIntake {
        return MedicationIntake(
            id = 2L,
            scheduledTime = scheduledTime,
            actualTime = null,
            isCompleted = false
        )
    }

    private class MockMedicationRepository : MedicationRepository {
        private val _lastCompletedIntake = MutableStateFlow<MedicationIntake?>(null)
        private val _nextScheduledIntake = MutableStateFlow<MedicationIntake?>(null)
        var allIntakes: List<MedicationIntake> = emptyList()

        var lastCompletedIntake: MedicationIntake?
            get() = _lastCompletedIntake.value
            set(value) { _lastCompletedIntake.value = value }

        var nextScheduledIntake: MedicationIntake?
            get() = _nextScheduledIntake.value
            set(value) { _nextScheduledIntake.value = value }

        override fun getAllIntakes() = flowOf(allIntakes)
        override fun getIntakesBetween(start: Instant, end: Instant) = flowOf(allIntakes)
        override fun getNextScheduledIntakeFlow() = _nextScheduledIntake
        override fun getLastCompletedIntakeFlow() = _lastCompletedIntake
        override suspend fun getIntakeById(id: Long) = allIntakes.find { it.id == id }
        override suspend fun getLatestIntake() = allIntakes.maxByOrNull { it.scheduledTime }
        override suspend fun getNextScheduledIntake() = nextScheduledIntake
        override suspend fun insertIntake(intake: MedicationIntake) = 0L
        override suspend fun updateIntake(intake: MedicationIntake) {}
        override suspend fun markIntakeTaken(intakeId: Long, actualTime: Instant) {}
        override suspend fun updateScheduledTime(intakeId: Long, newScheduledTime: Instant) {}
        override suspend fun submitFeedback(intakeId: Long, feedbackType: com.guanfancy.app.domain.model.FeedbackType, feedbackTime: Instant) {}
        override suspend fun deleteIntake(intake: MedicationIntake) {}
        override suspend fun deleteAllIntakes() {}
    }

    private class MockSettingsRepository(
        initialConfig: FoodZoneConfig
    ) : SettingsRepository {
        private val _config = MutableStateFlow(initialConfig)

        var config: FoodZoneConfig
            get() = _config.value
            set(value) { _config.value = value }

        override val foodZoneConfig = _config
        override val isWarningAccepted = flowOf(true)
        override val isOnboardingCompleted = flowOf(true)
        override val medicationType = flowOf(com.guanfancy.app.domain.model.MedicationType.INTUNIV)
        override val scheduleConfig = flowOf(com.guanfancy.app.domain.model.ScheduleConfig.DEFAULT)
        override val currentIntakeTime = flowOf<kotlinx.datetime.Instant?>(null)

        override suspend fun setWarningAccepted(accepted: Boolean) {}
        override suspend fun setOnboardingCompleted(completed: Boolean) {}
        override suspend fun setMedicationType(type: com.guanfancy.app.domain.model.MedicationType) {}
        override suspend fun updateScheduleConfig(config: com.guanfancy.app.domain.model.ScheduleConfig) {}
        override suspend fun setCurrentIntakeTime(time: kotlinx.datetime.Instant) {}
        override suspend fun clearAllData() {}
    }
}
