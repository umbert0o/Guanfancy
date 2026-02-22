package com.guanfancy.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.guanfancy.app.domain.model.FeedbackType

@Entity(tableName = "medication_intakes")
data class IntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduledTimeEpoch: Long,
    val actualTimeEpoch: Long? = null,
    val feedbackType: String? = null,
    val feedbackTimeEpoch: Long? = null,
    val nextScheduledTimeEpoch: Long? = null,
    val isCompleted: Boolean = false
)
