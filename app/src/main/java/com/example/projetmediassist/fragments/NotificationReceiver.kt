package com.example.projetmediassist.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.projetmediassist.R
import com.example.projetmediassist.utils.SettingsUtils

class NotificationReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {

        // 🧠 Vérifie si le mode "Ne pas déranger" est activé
        if (SettingsUtils.isDoNotDisturbEnabled(context)) {
            Log.d("NOTIF", "🔕 Notification bloquée (Ne pas déranger activé)")
            return
        }

        val title = intent.getStringExtra("title") ?: "Rendez-vous imminent"
        val message = intent.getStringExtra("message") ?: "Vous avez une consultation dans quelques minutes."

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d("NOTIF", "🔔 Notification reçue : $title - $message")

        // Crée le canal (Android 8+)
        val channelId = "rdv_channel"
        val channel = NotificationChannel(
            channelId,
            "Notifications RDV",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            // mets une icône existante
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notifId = System.currentTimeMillis().toInt()
        notificationManager.notify(notifId, builder.build())
    }
}


