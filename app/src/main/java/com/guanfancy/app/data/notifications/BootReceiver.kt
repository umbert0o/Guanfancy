package com.guanfancy.app.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule alarms after device reboot
            // This would need to be implemented with a WorkManager job
            // to reload and reschedule all pending reminders
        }
    }
}
