package com.example.projetmediassist.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.projetmediassist.fragments.NotificationReceiver
import java.util.*

object NotificationUtils {

    fun scheduleAppointmentNotifications(
        context: Context,
        appointmentTime: Long,
        patientName: String
    ) {
        val delays = listOf(30, 15, 5) // en minutes

        for (delay in delays) {
            val triggerTime = appointmentTime - delay * 60 * 1000L

            if (triggerTime > System.currentTimeMillis()) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("title", "Rendez-vous dans $delay min")
                    putExtra("message", "Consultation avec $patientName à venir")
                }

                val requestCode = (triggerTime / 1000).toInt() + delay
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                        Log.d("NOTIF", "✅ Alarme $delay min planifiée pour ${Date(triggerTime)}")
                    } else {
                        Log.w("NOTIF", "❌ Permission manquante pour planifier alarme exacte (API 31+)")
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d("NOTIF", "✅ Alarme $delay min planifiée pour ${Date(triggerTime)} (API < 31)")
                }
            }
        }
    }

    fun cancelAppointmentNotifications(context: Context, appointmentTime: Long) {
        val delays = listOf(30, 15, 5)

        for (delay in delays) {
            val triggerTime = appointmentTime - delay * 60 * 1000L
            val requestCode = (triggerTime / 1000).toInt() + delay

            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            Log.d("NOTIF", "🧼 Annulation alarme $delay min pour ${Date(triggerTime)}")
        }
    }


}
