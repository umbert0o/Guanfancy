package com.guanfancy.app.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guanfancy.app.R
import com.guanfancy.app.domain.model.MedicationType
import com.guanfancy.app.ui.components.FoodZoneExplanation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timePickerState = rememberTimePickerState(
        initialHour = state.selectedHour,
        initialMinute = state.selectedMinute,
        is24Hour = true
    )

    LaunchedEffect(timePickerState.hour) {
        viewModel.setHour(timePickerState.hour)
    }
    LaunchedEffect(timePickerState.minute) {
        viewModel.setMinute(timePickerState.minute)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Guanfancy",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Medication Tracking for Guanfacine",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (state.step) {
                0 -> WelcomeStep()
                1 -> MedicationTypeStep(
                    selectedType = state.medicationType,
                    onTypeSelected = viewModel::setMedicationType
                )
                2 -> TimeSelectionStep(timePickerState)
                3 -> ScheduleConfigStep(
                    config = state.scheduleConfig,
                    onGoodHoursChange = viewModel::setGoodHours,
                    onDizzyHoursChange = viewModel::setDizzyHours,
                    onTooDizzyHoursChange = viewModel::setTooDizzyHours,
                    onFeedbackDelayChange = viewModel::setFeedbackDelayHours
                )
                4 -> FoodZoneExplanationStep(config = state.medicationType.getFoodZoneConfig())
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.step > 0) {
                    OutlinedButton(onClick = { viewModel.previousStep() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (state.step < OnboardingViewModel.TOTAL_STEPS - 1) {
                    Button(onClick = { viewModel.nextStep() }) {
                        Text("Next")
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = { viewModel.completeOnboarding(onOnboardingComplete) },
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text("Get Started")
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app helps you track your Guanfacine medication intake.\n\n" +
                    "Key features:\n" +
                    "• Schedule medication reminders\n" +
                    "• Track how you feel after intake\n" +
                    "• Get food intake recommendations\n" +
                    "• Visualize your medication schedule",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MedicationTypeStep(
    selectedType: MedicationType,
    onTypeSelected: (MedicationType) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Which medication do you take?",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        MedicationType.entries.forEach { type ->
            MedicationTypeCard(
                type = type,
                isSelected = selectedType == type,
                onClick = { onTypeSelected(type) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This affects the food zone timing recommendations based on absorption rates.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MedicationTypeCard(
    type: MedicationType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = type.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (type) {
                    MedicationType.INTUNIV -> "Food zones: Red for 3h, Yellow for 5h after intake"
                    MedicationType.TENEX -> "Food zones: Red for 1.5h, Yellow for 2.5h after intake"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSelectionStep(timePickerState: androidx.compose.material3.TimePickerState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "When do you usually take your medication?",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        TimePicker(state = timePickerState)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This will be your default intake time. You can change it later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ScheduleConfigStep(
    config: com.guanfancy.app.domain.model.ScheduleConfig,
    onGoodHoursChange: (Int) -> Unit,
    onDizzyHoursChange: (Int) -> Unit,
    onTooDizzyHoursChange: (Int) -> Unit,
    onFeedbackDelayChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Schedule Settings",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Default values are pre-configured. Adjust if needed or keep as is.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = config.goodHours.toString(),
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let { onGoodHoursChange(it) }
            },
            label = { Text("Hours until next intake (Good)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = config.dizzyHours.toString(),
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let { onDizzyHoursChange(it) }
            },
            label = { Text("Hours until next intake (Dizzy)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = config.tooDizzyHours.toString(),
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let { onTooDizzyHoursChange(it) }
            },
            label = { Text("Hours until next intake (Too dizzy)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = config.feedbackDelayHours.toString(),
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let { onFeedbackDelayChange(it) }
            },
            label = { Text("Hours after intake to ask for feedback") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FoodZoneExplanationStep(config: com.guanfancy.app.domain.model.FoodZoneConfig) {
    FoodZoneExplanation(config = config)
}
