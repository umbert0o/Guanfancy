package com.guanfancy.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.guanfancy.app.domain.model.FoodZone
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.domain.model.IntakeSource
import com.guanfancy.app.domain.model.MedicationIntake
import com.guanfancy.app.ui.theme.FoodGreen
import com.guanfancy.app.ui.theme.FoodRed
import com.guanfancy.app.ui.theme.FoodYellow
import com.guanfancy.app.ui.theme.IntakeMarker
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class HourlyTimelineData(
    val date: LocalDate,
    val intakes: List<MedicationIntake>,
    val currentTime: Instant,
    val previousDayIntake: MedicationIntake? = null,
    val foodZoneConfig: FoodZoneConfig = FoodZoneConfig.DEFAULT
)

@Composable
fun HourlyTimeline(
    data: HourlyTimelineData,
    modifier: Modifier = Modifier,
    onHourLongPress: (Int) -> Unit = {},
    onIntakeTap: (MedicationIntake) -> Unit = {}
) {
    val timeZone = TimeZone.currentSystemDefault()
    
    val intakeInfoList = data.intakes.map { intake ->
        val displayTime = if (intake.isCompleted && intake.actualTime != null) {
            intake.actualTime
        } else {
            intake.scheduledTime
        }
        val localDateTime = displayTime.toLocalDateTime(timeZone)
        IntakeInfo(
            hour = localDateTime.time.hour,
            minute = localDateTime.time.minute,
            isCompleted = intake.isCompleted,
            displayTime = displayTime,
            intake = intake
        )
    }
    
    val previousIntakeDisplayTime = if (data.previousDayIntake?.isCompleted == true && data.previousDayIntake.actualTime != null) {
        data.previousDayIntake.actualTime
    } else {
        data.previousDayIntake?.scheduledTime
    }
    val previousIntakeHour = previousIntakeDisplayTime?.toLocalDateTime(timeZone)?.time?.hour

    val currentTimeLocal = data.currentTime.toLocalDateTime(timeZone)
    val isToday = currentTimeLocal.date == data.date
    val currentHour = currentTimeLocal.time.hour

    LazyColumn(modifier = modifier) {
        items((0..23).toList()) { hour ->
            val intakeAtHour = intakeInfoList.find { it.hour == hour }
            HourRow(
                hour = hour,
                isIntakeHour = intakeAtHour != null,
                isCompleted = intakeAtHour?.isCompleted == true,
                isManual = intakeAtHour?.intake?.source == IntakeSource.MANUAL,
                isCurrentHour = isToday && hour == currentHour,
                foodZone = calculateFoodZoneForHour(
                    hour = hour,
                    intakeHours = intakeInfoList.map { it.hour },
                    previousIntakeHour = previousIntakeHour,
                    config = data.foodZoneConfig
                ),
                intakeMinute = intakeAtHour?.minute ?: 0,
                onLongPress = { onHourLongPress(hour) },
                onIntakeTap = { intakeAtHour?.let { onIntakeTap(it.intake) } }
            )
        }
    }
}

private data class IntakeInfo(
    val hour: Int,
    val minute: Int,
    val isCompleted: Boolean,
    val displayTime: Instant,
    val intake: MedicationIntake
)

private fun calculateFoodZoneForHour(
    hour: Int,
    intakeHours: List<Int>,
    previousIntakeHour: Int?,
    config: FoodZoneConfig
): FoodZone {
    if (intakeHours.isEmpty() && previousIntakeHour == null) return FoodZone.GREEN

    val zones = mutableListOf<FoodZone>()
    
    for (intakeHour in intakeHours) {
        val zone = calculateZoneRelativeToSingleIntake(hour, intakeHour, config)
        zones.add(zone)
    }
    
    if (previousIntakeHour != null) {
        val hoursSincePreviousIntake = (24 - previousIntakeHour) + hour
        val postZone = when {
            hoursSincePreviousIntake <= config.redHoursAfter -> FoodZone.RED
            hoursSincePreviousIntake <= config.yellowHoursAfter -> FoodZone.YELLOW
            else -> FoodZone.GREEN
        }
        zones.add(postZone)
    }
    
    return zones.minByOrNull { zone ->
        when (zone) {
            FoodZone.RED -> 0
            FoodZone.YELLOW -> 1
            FoodZone.GREEN -> 2
        }
    } ?: FoodZone.GREEN
}

private fun calculateZoneRelativeToSingleIntake(
    hour: Int,
    intakeHour: Int,
    config: FoodZoneConfig
): FoodZone {
    return when {
        hour < intakeHour -> {
            val hoursBefore = intakeHour - hour
            when {
                hoursBefore <= config.yellowHoursBefore -> FoodZone.RED
                hoursBefore <= config.greenHoursBefore -> FoodZone.YELLOW
                else -> FoodZone.GREEN
            }
        }
        hour == intakeHour -> FoodZone.RED
        else -> {
            val hoursAfter = hour - intakeHour
            when {
                hoursAfter <= config.redHoursAfter -> FoodZone.RED
                hoursAfter <= config.yellowHoursAfter -> FoodZone.YELLOW
                else -> FoodZone.GREEN
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HourRow(
    hour: Int,
    isIntakeHour: Boolean,
    isCompleted: Boolean,
    isManual: Boolean,
    isCurrentHour: Boolean,
    foodZone: FoodZone,
    intakeMinute: Int,
    onLongPress: () -> Unit,
    onIntakeTap: () -> Unit
) {
    val backgroundColor = when {
        isCurrentHour -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> when (foodZone) {
            FoodZone.GREEN -> FoodGreen.copy(alpha = 0.1f)
            FoodZone.YELLOW -> FoodYellow.copy(alpha = 0.1f)
            FoodZone.RED -> FoodRed.copy(alpha = 0.1f)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = {},
                onLongClick = { onLongPress() }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${hour.toString().padStart(2, '0')}:00",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCurrentHour) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(48.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    when (foodZone) {
                        FoodZone.GREEN -> FoodGreen.copy(alpha = 0.2f)
                        FoodZone.YELLOW -> FoodYellow.copy(alpha = 0.3f)
                        FoodZone.RED -> FoodRed.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isIntakeHour) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isManual) MaterialTheme.colorScheme.secondary else IntakeMarker
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .combinedClickable(
                            onClick = { if (isManual) onIntakeTap() },
                            onLongClick = null
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isManual) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Manual intake",
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, MaterialTheme.shapes.small)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCompleted) "Taken at ${hour.toString().padStart(2, '0')}:${intakeMinute.toString().padStart(2, '0')}"
                                   else "Intake at ${hour.toString().padStart(2, '0')}:${intakeMinute.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }

            if (isCurrentHour && !isIntakeHour) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.error)
                )
            }
        }
    }
}

@Composable
fun TimelineLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = FoodGreen, label = "Green: OK to eat")
        Spacer(modifier = Modifier.width(16.dp))
        LegendItem(color = FoodYellow, label = "Yellow: Caution")
        Spacer(modifier = Modifier.width(16.dp))
        LegendItem(color = FoodRed, label = "Red: Avoid eating")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, shape = MaterialTheme.shapes.small)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
