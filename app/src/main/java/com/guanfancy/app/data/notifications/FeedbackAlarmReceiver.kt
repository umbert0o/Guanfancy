package com.guanfancy.app.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeedbackAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val intakeId = intent.getLongExtra(NotificationHelper.EXTRA_INTAKE_ID, 0)
        if (intakeId > 0) {
            notificationHelper.showFeedbackNotification(intakeId)
        }
    }
}
