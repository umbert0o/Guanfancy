package com.guanfancy.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guanfancy.app.domain.model.FoodZoneConfig
import com.guanfancy.app.ui.theme.FoodGreen
import com.guanfancy.app.ui.theme.FoodRed
import com.guanfancy.app.ui.theme.FoodYellow

@Composable
fun FoodZoneExplanation(
    config: FoodZoneConfig = FoodZoneConfig.DEFAULT,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Food Intake Zones",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Guanfacine interacts with food. Avoid eating close to intake time:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        ZoneCard(
            color = FoodGreen,
            title = "Green Zone",
            description = "More than ${config.greenHoursBefore}h before or ${config.yellowHoursAfter}h+ after intake"
        )

        Spacer(modifier = Modifier.height(8.dp))

        ZoneCard(
            color = FoodYellow,
            title = "Yellow Zone",
            description = "${config.yellowHoursBefore}-${config.greenHoursBefore}h before or ${config.redHoursAfter}-${config.yellowHoursAfter}h after intake"
        )

        Spacer(modifier = Modifier.height(8.dp))

        ZoneCard(
            color = FoodRed,
            title = "Red Zone",
            description = "Less than ${config.yellowHoursBefore}h before or ${config.redHoursAfter}h after intake"
        )
    }
}

@Composable
private fun ZoneCard(
    color: androidx.compose.ui.graphics.Color,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "●",
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun FoodZoneHelpDialog(
    config: FoodZoneConfig = FoodZoneConfig.DEFAULT,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        },
        text = {
            FoodZoneExplanation(config = config)
        }
    )
}
