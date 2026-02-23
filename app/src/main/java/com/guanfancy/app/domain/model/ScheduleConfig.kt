package com.guanfancy.app.domain.model

data class ScheduleConfig(
    val feedbackDelayHours: Int = 12,
    val defaultIntakeTimeHour: Int = 8,
    val defaultIntakeTimeMinute: Int = 0
) {
    fun getDelayHoursForFeedback(feedbackType: FeedbackType): Int {
        return when (feedbackType) {
            FeedbackType.GOOD -> 0
            FeedbackType.DIZZY -> DIZZY_DELAY_HOURS
            FeedbackType.TOO_DIZZY -> TOO_DIZZY_DELAY_HOURS
        }
    }

    companion object {
        val DEFAULT = ScheduleConfig()
        const val RESCHEDULE_WINDOW_HOURS = 2
        const val DIZZY_DELAY_HOURS = 12
        const val TOO_DIZZY_DELAY_HOURS = 24
    }
}
