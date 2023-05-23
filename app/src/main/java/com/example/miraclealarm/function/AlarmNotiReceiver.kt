package com.example.miraclealarm.function

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.miraclealarm.R
import com.example.miraclealarm.activity.MainActivity

class AlarmNotiReceiver : BroadcastReceiver() {

    lateinit var manager: NotificationManager
    lateinit var builder: NotificationCompat.Builder

    private val channel_id = "channel_id"
    private val channel_name = "channel_name"

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val contentValue = intent.getStringExtra("content")
        Log.d("confirm contentValue", "confirm contentValue : $contentValue")

        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                channel_id,
                channel_name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        builder = NotificationCompat.Builder(context, channel_id)

        val intent2 = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent2,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        builder.setContentTitle(contentValue)
        //builder.setContentText(contentValue)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        builder.setAutoCancel(true)
        builder.setContentIntent(pendingIntent)

        val notification = builder.build()
        manager.notify(1, notification)
    }
}