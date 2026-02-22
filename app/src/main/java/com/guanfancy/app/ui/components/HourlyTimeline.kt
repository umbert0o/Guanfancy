package com.guanfancy.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.guanfancy.app.domain.model.FoodZone
import com.guanfancy.app.domain.model.GuanfacineConstants
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
    val previousDayIntake: MedicationIntake? = null
)

@Composable
fun HourlyTimeline(
    data: HourlyTimelineData,
    modifier: Modifier = Modifier,
    onHourTap: (Int) -> Unit = {}
) {
    val timeZone = TimeZone.currentSystemDefault()
    val nextIntake = data.intakes.firstOrNull { !it.isCompleted && it.scheduledTime > data.currentTime }
    val intakeHour = nextIntake?.scheduledTime?.toLocalDateTime(timeZone)?.time?.hour
    val previousIntakeHour = data.previousDayIntake?.scheduledTime?.toLocalDateTime(timeZone)?.time?.hour

    val currentTimeLocal = data.currentTime.toLocalDateTime(timeZone)
    val isToday = currentTimeLocal.date == data.date
    val currentHour = currentTimeLocal.time.hour

    LazyColumn(modifier = modifier) {
        items((0..23).toList()) { hour ->
            HourRow(
                hour = hour,
                isIntakeHour = hour == intakeHour,
                isCurrentHour = isToday && hour == currentHour,
                foodZone = calculateFoodZone(hour, intakeHour, previousIntakeHour),
                intakeMinute = if (hour == intakeHour) {
                    nextIntake?.scheduledTime?.toLocalDateTime(timeZone)?.time?.minute ?: 0
                } else 0,
                onTap = { onHourTap(hour) }
            )
        }
    }
}

private fun calculateFoodZone(hour: Int?, intakeHour: Int?, previousIntakeHour: Int?): FoodZone {
    if (hour == null) return FoodZone.GREEN

    val preIntakeZone = if (intakeHour != null) {
        when {
            hour < intakeHour - GuanfacineConstants.FOOD_GREEN_HOURS_BEFORE -> FoodZone.GREEN
            hour < intakeHour - GuanfacineConstants.FOOD_YELLOW_HOURS_BEFORE -> FoodZone.YELLOW
            hour < intakeHour -> FoodZone.RED
            hour == intakeHour -> FoodZone.RED
            else -> {
                val hoursAfterIntake = hour - intakeHour
                when {
                    hoursAfterIntake < GuanfacineConstants.FOOD_RED_HOURS_AFTER -> FoodZone.RED
                    hoursAfterIntake < GuanfacineConstants.FOOD_YELLOW_HOURS_AFTER -> FoodZone.YELLOW
                    else -> FoodZone.GREEN
                }
            }
        }
    } else null

    val postIntakeZone = if (previousIntakeHour != null) {
        val hoursSincePreviousIntake = (24 - previousIntakeHour) + hour
        when {
            hoursSincePreviousIntake < GuanfacineConstants.FOOD_RED_HOURS_AFTER -> FoodZone.RED
            hoursSincePreviousIntake < GuanfacineConstants.FOOD_YELLOW_HOURS_AFTER -> FoodZone.YELLOW
            else -> FoodZone.GREEN
        }
    } else null

    return listOfNotNull(preIntakeZone, postIntakeZone).minByOrNull { zone ->
        when (zone) {
            FoodZone.RED -> 0
            FoodZone.YELLOW -> 1
            FoodZone.GREEN -> 2
        }
    } ?: FoodZone.GREEN
}

@Composable
private fun HourRow(
    hour: Int,
    isIntakeHour: Boolean,
    isCurrentHour: Boolean,
    foodZone: FoodZone,
    intakeMinute: Int,
    onTap: () -> Unit
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
                    colors = CardDefaults.cardColors(containerColor = IntakeMarker),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, MaterialTheme.shapes.small)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Intake at ${hour.toString().padStart(2, '0')}:${intakeMinute.toString().padStart(2, '0')}",
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
