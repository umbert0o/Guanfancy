package com.guanfancy.app.domain.model

data class ScheduleConfig(
    val goodHours: Int = 24,
    val dizzyHours: Int = 36,
    val tooDizzyHours: Int = 48,
    val feedbackDelayHours: Int = 12,
    val defaultIntakeTimeHour: Int = 8,
    val defaultIntakeTimeMinute: Int = 0
) {
    fun getHoursForFeedback(feedbackType: FeedbackType): Int {
        return when (feedbackType) {
            FeedbackType.GOOD -> goodHours
            FeedbackType.DIZZY -> dizzyHours
            FeedbackType.TOO_DIZZY -> tooDizzyHours
        }
    }

    companion object {
        val DEFAULT = ScheduleConfig()
    }
}
