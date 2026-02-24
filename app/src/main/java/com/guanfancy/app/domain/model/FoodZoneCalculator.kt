package com.guanfancy.app.domain.model

import kotlinx.datetime.Instant

object FoodZoneCalculator {

    fun calculate(
        now: Instant,
        lastTakenTime: Instant?,
        nextScheduledTime: Instant?,
        config: FoodZoneConfig
    ): FoodZone {
        val afterZone = calculateAfterZone(now, lastTakenTime, config)
        val beforeZone = calculateBeforeZone(now, nextScheduledTime, config)

        return when {
            afterZone == FoodZone.RED || beforeZone == FoodZone.RED -> FoodZone.RED
            afterZone == FoodZone.YELLOW || beforeZone == FoodZone.YELLOW -> FoodZone.YELLOW
            else -> FoodZone.GREEN
        }
    }

    private fun calculateAfterZone(now: Instant, lastTakenTime: Instant?, config: FoodZoneConfig): FoodZone? {
        if (lastTakenTime == null) return null

        val hoursSinceTaken = (now.toEpochMilliseconds() - lastTakenTime.toEpochMilliseconds()) / (1000.0 * 60 * 60)

        return when {
            hoursSinceTaken < config.redHoursAfter -> FoodZone.RED
            hoursSinceTaken < config.yellowHoursAfter -> FoodZone.YELLOW
            else -> null
        }
    }

    private fun calculateBeforeZone(now: Instant, nextScheduledTime: Instant?, config: FoodZoneConfig): FoodZone? {
        if (nextScheduledTime == null) return null

        val hoursUntilScheduled = (nextScheduledTime.toEpochMilliseconds() - now.toEpochMilliseconds()) / (1000.0 * 60 * 60)

        return when {
            hoursUntilScheduled < config.yellowHoursBefore -> FoodZone.RED
            hoursUntilScheduled < config.greenHoursBefore -> FoodZone.YELLOW
            else -> null
        }
    }
}
