package com.guanfancy.app.domain.model

import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class IntakeTimingResult(
    val needsReschedulePrompt: Boolean,
    val nextIntakeTime: Instant? = null
)

object IntakeTimingCalculator {

    fun calculate(
        now: Instant,
        defaultHour: Int,
        defaultMinute: Int,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): IntakeTimingResult {
        val today = now.toLocalDateTime(timeZone).date
        val defaultTime = LocalTime(defaultHour, defaultMinute)
        
        val todayDefaultInstant = defaultTime.atDate(today).toInstant(timeZone)
        val windowStart = todayDefaultInstant - ScheduleConfig.RESCHEDULE_WINDOW_HOURS.hours
        val windowEnd = todayDefaultInstant + ScheduleConfig.RESCHEDULE_WINDOW_HOURS.hours
        
        val tomorrowDefaultInstant = todayDefaultInstant.plus(1, DateTimeUnit.DAY, timeZone)
        
        return when {
            now >= windowStart && now <= windowEnd -> {
                val nextTime = if (now < todayDefaultInstant) todayDefaultInstant else tomorrowDefaultInstant
                IntakeTimingResult(
                    needsReschedulePrompt = false,
                    nextIntakeTime = nextTime
                )
            }
            else -> IntakeTimingResult(needsReschedulePrompt = true)
        }
    }

    fun calculateNextDefaultTime(
        now: Instant,
        defaultHour: Int,
        defaultMinute: Int,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Instant {
        val today = now.toLocalDateTime(timeZone).date
        val defaultTime = LocalTime(defaultHour, defaultMinute)
        val todayDefaultInstant = defaultTime.atDate(today).toInstant(timeZone)
        
        return if (now < todayDefaultInstant) {
            todayDefaultInstant
        } else {
            todayDefaultInstant.plus(1, DateTimeUnit.DAY, timeZone)
        }
    }

    fun applyFeedbackDelay(
        baseTime: Instant,
        delayHours: Int,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Instant {
        return baseTime + delayHours.hours
    }
}
