package com.example.caraocruz.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.caraocruz.R

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "victoria_channel"
        private const val CHANNEL_NAME = "Notificaciones de Victoria"
        private const val CHANNEL_DESC = "Muestra una notificación cuando ganas una partida"
        private const val NOTIFICATION_ID = 101
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showVictoryNotification(apuesta: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logocaraocruz)
            .setContentTitle(context.getString(R.string.notif_victory_title))
            .setContentText(context.getString(R.string.notif_victory_text, apuesta))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            // Manejar falta de permisos si es necesario
        }
    }
}
