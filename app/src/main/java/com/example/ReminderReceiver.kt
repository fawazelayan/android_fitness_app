package com.example

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.AppDatabase
import com.example.data.IntakeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule alarm if it was configured as enabled in DB
            val db = AppDatabase.getDatabase(context)
            val repo = IntakeRepository(db.intakeDao)
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = repo.getSettingBoolean(IntakeRepository.KEY_REMINDERS_ENABLED, false)
                if (enabled) {
                    val hour = repo.getSettingInt(IntakeRepository.KEY_REMINDER_HOUR, 20)
                    val minute = repo.getSettingInt("reminder_minute", 0)
                    scheduleDailyReminder(context, hour, minute)
                }
                val waterEnabled = repo.getSettingBoolean(IntakeRepository.KEY_WATER_REMINDERS_ENABLED, false)
                if (waterEnabled) {
                    val hour = repo.getSettingInt(IntakeRepository.KEY_WATER_REMINDER_HOUR, 10)
                    val minute = repo.getSettingInt(IntakeRepository.KEY_WATER_REMINDER_MINUTE, 0)
                    scheduleDailyWaterReminder(context, hour, minute)
                }
            }
            return
        }

        if (action == ACTION_WATER_REMINDER) {
            showWaterNotification(context)

            // Reschedule water alarm for next day
            val db = AppDatabase.getDatabase(context)
            val repo = IntakeRepository(db.intakeDao)
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = repo.getSettingBoolean(IntakeRepository.KEY_WATER_REMINDERS_ENABLED, false)
                if (enabled) {
                    val hour = repo.getSettingInt(IntakeRepository.KEY_WATER_REMINDER_HOUR, 10)
                    val minute = repo.getSettingInt(IntakeRepository.KEY_WATER_REMINDER_MINUTE, 0)
                    scheduleDailyWaterReminder(context, hour, minute)
                }
            }
            return
        }

        // Trigger notification
        showNotification(context)

        // Reschedule for the next day to maintain the exact daily reminders chain
        val db = AppDatabase.getDatabase(context)
        val repo = IntakeRepository(db.intakeDao)
        CoroutineScope(Dispatchers.IO).launch {
            val enabled = repo.getSettingBoolean(IntakeRepository.KEY_REMINDERS_ENABLED, false)
            if (enabled) {
                val hour = repo.getSettingInt(IntakeRepository.KEY_REMINDER_HOUR, 20)
                val minute = repo.getSettingInt("reminder_minute", 0)
                scheduleDailyReminder(context, hour, minute)
            }
        }
    }

    private fun showWaterNotification(context: Context) {
        val channelId = "water_intake_reminders"
        val notificationId = 882

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ringerUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Water Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to record your water intake"
                setSound(ringerUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Stay Hydrated! 💧")
            .setContentText("Keep up with your goals! Remember to log your water intake today.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(ringerUri)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
        Log.d("ReminderReceiver", "Water alert notification posted successfully.")
    }

    private fun showNotification(context: Context) {
        val channelId = "scoop_tracker_reminders_v2"
        val notificationId = 881

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ringerUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Intake Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to record your creatine and protein intakes"
                setSound(ringerUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Re-open application on click
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Track Your Daily Scoops! 💪")
            .setContentText("Stay on track with your goals! Remember to log your creatine and protein scoop count today.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(ringerUri)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
        Log.d("ReminderReceiver", "Alert notification posted successfully.")
    }

    companion object {
        const val ACTION_WATER_REMINDER = "com.example.ACTION_WATER_REMINDER"
        private const val ALARM_REQ_CODE = 455
        private const val ALARM_REQ_CODE_WATER = 456

        fun scheduleDailyWaterReminder(context: Context, hour: Int = 10, minute: Int = 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_WATER_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQ_CODE_WATER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        Log.d("ReminderReceiver", "Exact water alarm set for $hour:$minute")
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Could not register water reminder broadcast alarm: ${e.message}")
            }
        }

        fun cancelDailyWaterReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_WATER_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQ_CODE_WATER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("ReminderReceiver", "Water alarm canceled successfully.")
        }

        fun scheduleDailyReminder(context: Context, hour: Int = 20, minute: Int = 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQ_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Dynamic schedule setting for custom hour and minute local time
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        Log.d("ReminderReceiver", "Exact alarm set for $hour:$minute using setExactAndAllowWhileIdle")
                    } else {
                        // Fallback to normal exact or window/inexact
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        Log.d("ReminderReceiver", "canScheduleExactAlarms is false. Fallback setAndAllowWhileIdle set for $hour:$minute")
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderReceiver", "Exact alarm set for $hour:$minute using setExactAndAllowWhileIdle (API < 31)")
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderReceiver", "Exact alarm set for $hour:$minute using setExact (API < 23)")
                }
            } catch (e: SecurityException) {
                try {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderReceiver", "SecurityException. Fallback setAndAllowWhileIdle set for $hour:$minute")
                } catch (ex: Exception) {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("ReminderReceiver", "SecurityException. Fallback set set for $hour:$minute")
                }
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Could not register reminder broadcast alarm: ${e.message}")
            }
        }

        fun cancelDailyReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQ_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("ReminderReceiver", "Repeating alarm canceled successfully.")
        }
    }
}
