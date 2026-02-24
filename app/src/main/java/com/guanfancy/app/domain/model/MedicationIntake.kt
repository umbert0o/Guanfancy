package com.guanfancy.app.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime

data class MedicationIntake(
    val id: Long = 0,
    val scheduledTime: Instant,
    val actualTime: Instant? = null,
    val feedback: FeedbackType? = null,
    val feedbackTime: Instant? = null,
    val nextScheduledTime: Instant? = null,
    val isCompleted: Boolean = false,
    val source: IntakeSource = IntakeSource.SCHEDULED
)
