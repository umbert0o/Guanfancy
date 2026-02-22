package com.guanfancy.app.ui.screens.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guanfancy.app.domain.model.FeedbackType
import com.guanfancy.app.ui.theme.FoodGreen
import com.guanfancy.app.ui.theme.FoodRed
import com.guanfancy.app.ui.theme.FoodYellow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onFeedbackComplete: () -> Unit,
    intakeId: Long = 0,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(intakeId) {
        if (intakeId > 0) {
            viewModel.loadIntake(intakeId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How are you feeling?") },
                navigationIcon = {
                    IconButton(onClick = onFeedbackComplete) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "How did you feel after your last medication intake?",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeedbackOption(
                        modifier = Modifier.weight(1f),
                        label = "Good",
                        description = "No issues",
                        color = FoodGreen,
                        selected = state.selectedFeedback == FeedbackType.GOOD,
                        onClick = { viewModel.selectFeedback(FeedbackType.GOOD) }
                    )

                    FeedbackOption(
                        modifier = Modifier.weight(1f),
                        label = "Dizzy",
                        description = "Some dizziness",
                        color = FoodYellow,
                        selected = state.selectedFeedback == FeedbackType.DIZZY,
                        onClick = { viewModel.selectFeedback(FeedbackType.DIZZY) }
                    )

                    FeedbackOption(
                        modifier = Modifier.weight(1f),
                        label = "Too Dizzy",
                        description = "Significant issues",
                        color = FoodRed,
                        selected = state.selectedFeedback == FeedbackType.TOO_DIZZY,
                        onClick = { viewModel.selectFeedback(FeedbackType.TOO_DIZZY) }
                    )
                }

                state.nextScheduledTime?.let { nextTime ->
                    Spacer(modifier = Modifier.height(24.dp))

                    val localTime = nextTime.toLocalDateTime(TimeZone.currentSystemDefault())
                    Text(
                        text = "Next intake scheduled for: ${localTime.date} at ${localTime.time.hour.toString().padStart(2, '0')}:${localTime.time.minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.submitFeedback(onFeedbackComplete) },
                    enabled = state.selectedFeedback != null && !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Submit Feedback")
                }
            }
        }
    }
}

@Composable
private fun FeedbackOption(
    label: String,
    description: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
