package com.guanfancy.app.data.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.guanfancy.app.MainActivity
import com.guanfancy.app.R
import com.guanfancy.app.domain.model.ScheduleConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_FEEDBACK = "feedback_channel"
        const val CHANNEL_INTAKE = "intake_channel"
        const val NOTIFICATION_FEEDBACK_ID = 1001
        const val NOTIFICATION_INTAKE_ID = 1002
        const val EXTRA_INTAKE_ID = "intake_id"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val TYPE_FEEDBACK = "feedback"
        const val TYPE_INTAKE = "intake"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val feedbackChannel = NotificationChannel(
                CHANNEL_FEEDBACK,
                context.getString(R.string.channel_feedback),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for medication feedback reminders"
            }

            val intakeChannel = NotificationChannel(
                CHANNEL_INTAKE,
                context.getString(R.string.channel_intake),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medication intake reminders"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(feedbackChannel, intakeChannel))
        }
    }

    fun showFeedbackNotification(intakeId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_FEEDBACK)
            putExtra(EXTRA_INTAKE_ID, intakeId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            intakeId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FEEDBACK)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_feedback_title))
            .setContentText(context.getString(R.string.notification_feedback_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_FEEDBACK_ID, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    fun showIntakeNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_INTAKE)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_INTAKE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_intake_title))
            .setContentText(context.getString(R.string.notification_intake_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_INTAKE_ID, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    fun scheduleFeedbackReminder(intakeId: Long, delayHours: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, FeedbackAlarmReceiver::class.java).apply {
            putExtra(EXTRA_INTAKE_ID, intakeId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intakeId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + (delayHours * 60 * 60 * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Cannot schedule alarms
        }
    }

    fun scheduleIntakeReminder(triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, IntakeAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_INTAKE_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Cannot schedule alarms
        }
    }

    fun cancelFeedbackReminder(intakeId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, FeedbackAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intakeId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}
