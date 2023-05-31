package com.grusie.miraclealarm.function

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.activity.NotificationActivity
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class ForegroundAlarmService : Service() {
    private lateinit var wakeLock: PowerManager.WakeLock
    private var mp: MediaPlayer? = null
    private var NOTIFICATION_ID by Delegates.notNull<Int>()
    private val CHANNEL_ID = "channel_id"
    private val CHANNEL_NAME = "channel_name"

    override fun onCreate() {
        super.onCreate()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE, "app:com.grusie.miraclealarm"
        )

        wakeLock.acquire()
    }

    override fun onDestroy() {
        super.onDestroy()
        mp?.stop()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("alarmId", -1)
        val title = intent?.getStringExtra("title")
        val content = intent?.getStringExtra("contentValue")

        val alarmDao = AlarmDatabase.getDatabase(applicationContext).alarmDao()
        val repository = AlarmRepository(alarmDao)

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                if (alarmId != null) {
                    val alarm = repository.getAlarmById(alarmId)

                    Log.d("confirm alarmData", "$alarm")
                    if (alarm.flagSound) {
                        val sound = Utils.getAlarmSound(applicationContext, alarm.sound)
                        mp = MediaPlayer.create(applicationContext, sound)
                        mp?.isLooping = true
                        mp?.start()
                    }
                }
            }
        }

        NOTIFICATION_ID = System.currentTimeMillis().toInt()

        createNotificationChannel()

        val notificationIntent = Intent(this, NotificationActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationIntent.putExtra("alarmId", alarmId)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(
            "confirm contentValue",
            "NOTIFICATION_ID : $NOTIFICATION_ID, confirm contentValue : $content"
        )
        val notification = createNotification(title, content, pendingIntent, pendingIntent)

        startForeground(NOTIFICATION_ID, notification)
        startActivity(notificationIntent)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification(
        title: String?,
        content: String?,
        pendingIntent: PendingIntent,
        fullScreenPendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
//            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}