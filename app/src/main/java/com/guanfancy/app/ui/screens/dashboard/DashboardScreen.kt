package com.guanfancy.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guanfancy.app.ui.components.FoodZoneIndicator
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showRescheduleDialog) {
        RescheduleDefaultTimeDialog(
            onConfirm = { viewModel.confirmRescheduleDefaultTime() },
            onDismiss = { viewModel.declineRescheduleDefaultTime() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guanfancy") },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FoodZoneIndicator(
                    zone = state.currentFoodZone,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                state.nextIntake?.let { intake ->
                    NextIntakeCard(
                        scheduledTime = intake.scheduledTime,
                        timeUntilIntake = state.timeUntilIntake,
                        onTakeMedication = { 
                            viewModel.markIntakeTaken(
                                onShowReschedulePrompt = {}
                            )
                        },
                        onReschedule = { newTime -> viewModel.rescheduleNextIntake(newTime) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } ?: run {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No upcoming intake scheduled",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RescheduleDefaultTimeDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule default intake time?") },
        text = {
            Text("You're taking your medication outside your usual time window. Would you like to update your default intake time to now?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Yes, reschedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No, keep current")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NextIntakeCard(
    scheduledTime: Instant,
    timeUntilIntake: Long,
    onTakeMedication: () -> Unit,
    onReschedule: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val localTime = scheduledTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()

    val today = now.toLocalDateTime(timeZone).date
    val maxDate = today.plus(3, DateTimeUnit.DAY)

    val timePickerState = rememberTimePickerState(
        initialHour = localTime.time.hour,
        initialMinute = localTime.time.minute,
        is24Hour = true
    )

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = scheduledTime.toEpochMilliseconds(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val dateInstant = Instant.fromEpochMilliseconds(utcTimeMillis)
                val dateLocal = dateInstant.toLocalDateTime(timeZone).date
                return dateLocal >= today && dateLocal <= maxDate
            }
        }
    )

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Next Intake",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${localTime.date.dayOfMonth.toString().padStart(2, '0')}.${localTime.date.monthNumber.toString().padStart(2, '0')}. ${localTime.time.hour.toString().padStart(2, '0')}:${localTime.time.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit time",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val hoursRemaining = (timeUntilIntake / (1000 * 60 * 60)).absoluteValue
            val minutesRemaining = ((timeUntilIntake / (1000 * 60)) % 60).absoluteValue

            Text(
                text = if (timeUntilIntake > 0) {
                    "In $hoursRemaining h $minutesRemaining min"
                } else {
                    "Overdue by $hoursRemaining h $minutesRemaining min"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (timeUntilIntake > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onTakeMedication,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Take Medication Now")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            selectedDate = instant.toLocalDateTime(timeZone).date
                            showDatePicker = false
                            showTimePicker = true
                        }
                    }
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select Date",
                        modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)
                    )
                },
                headline = {
                    Text(
                        text = "Up to 3 days ahead",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }

    if (showTimePicker) {
        selectedDate?.let { date ->
            TimePickerDialog(
                timePickerState = timePickerState,
                onDismiss = { 
                    showTimePicker = false
                    selectedDate = null
                },
                onConfirm = { hour, minute ->
                    val newDateTime = date.atTime(hour, minute)
                    val newInstant = newDateTime.toInstant(timeZone)
                    
                    if (newInstant > now) {
                        onReschedule(newInstant)
                    }
                    showTimePicker = false
                    selectedDate = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Adjust Next Intake Time",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select a future time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TimePicker(state = timePickerState)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
