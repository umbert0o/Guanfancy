package com.guanfancy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.guanfancy.app.ui.theme.FoodGreen
import com.guanfancy.app.ui.theme.FoodRed
import com.guanfancy.app.ui.theme.FoodYellow

@Composable
fun FoodZoneIndicator(
    zone: FoodZone,
    modifier: Modifier = Modifier
) {
    val (color, backgroundColor, text, description) = when (zone) {
        FoodZone.GREEN -> Tuple4(FoodGreen, FoodGreen.copy(alpha = 0.2f), "Green Zone", "Eating is fine")
        FoodZone.YELLOW -> Tuple4(FoodYellow, FoodYellow.copy(alpha = 0.2f), "Yellow Zone", "Caution - avoid large meals")
        FoodZone.RED -> Tuple4(FoodRed, FoodRed.copy(alpha = 0.2f), "Red Zone", "Avoid eating")
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (zone) {
                        FoodZone.GREEN -> "●"
                        FoodZone.YELLOW -> "●"
                        FoodZone.RED -> "●"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
