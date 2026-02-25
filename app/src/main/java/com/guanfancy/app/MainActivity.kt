package com.guanfancy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.guanfancy.app.data.notifications.NotificationHelper
import com.guanfancy.app.ui.navigation.GuanfancyNavHost
import com.guanfancy.app.ui.theme.GuanfancyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var deepLinkIntakeId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            GuanfancyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GuanfancyNavHost(deepLinkIntakeId = deepLinkIntakeId)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent) {
        val notificationType = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE)
        if (notificationType == NotificationHelper.TYPE_FEEDBACK) {
            val intakeId = intent.getLongExtra(NotificationHelper.EXTRA_INTAKE_ID, 0)
            if (intakeId > 0) {
                deepLinkIntakeId = intakeId
            }
        }
    }
}
