package com.guanfancy.app.data.local.entity

import com.guanfancy.app.domain.model.FeedbackType
import com.guanfancy.app.domain.model.MedicationIntake
import kotlinx.datetime.Instant

fun IntakeEntity.toDomain(): MedicationIntake {
    return MedicationIntake(
        id = id,
        scheduledTime = Instant.fromEpochMilliseconds(scheduledTimeEpoch),
        actualTime = actualTimeEpoch?.let { Instant.fromEpochMilliseconds(it) },
        feedback = feedbackType?.let { FeedbackType.valueOf(it) },
        feedbackTime = feedbackTimeEpoch?.let { Instant.fromEpochMilliseconds(it) },
        nextScheduledTime = nextScheduledTimeEpoch?.let { Instant.fromEpochMilliseconds(it) },
        isCompleted = isCompleted
    )
}

fun MedicationIntake.toEntity(): IntakeEntity {
    return IntakeEntity(
        id = id,
        scheduledTimeEpoch = scheduledTime.toEpochMilliseconds(),
        actualTimeEpoch = actualTime?.toEpochMilliseconds(),
        feedbackType = feedback?.name,
        feedbackTimeEpoch = feedbackTime?.toEpochMilliseconds(),
        nextScheduledTimeEpoch = nextScheduledTime?.toEpochMilliseconds(),
        isCompleted = isCompleted
    )
}
