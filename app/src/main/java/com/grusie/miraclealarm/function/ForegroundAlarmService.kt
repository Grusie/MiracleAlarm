package com.grusie.miraclealarm.function

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.activity.MainActivity
import com.grusie.miraclealarm.activity.NotificationActivity
import com.grusie.miraclealarm.function.Utils.Companion.changeVolume
import com.grusie.miraclealarm.model.AlarmData
import kotlin.properties.Delegates

class ForegroundAlarmService : Service(), HeadsetReceiver.HeadsetConnectionListener {
    private var NOTIFICATION_ID by Delegates.notNull<Int>()
    private val CHANNEL_ID = "channel_id"
    private val CHANNEL_NAME = "channel_name"
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var headsetReceiver = HeadsetReceiver()
    private lateinit var alarm: AlarmData
    private var isConnected = false
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(headsetReceiver)
        } catch (e: Exception) {
        }
        Utils.stopAlarmSound(this)
        changeVolume(this, null, isConnected)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        NOTIFICATION_ID = System.currentTimeMillis().toInt()

        if (intent?.action == Const.ACTION_MISSED_ALARM) {
            val count = intent.getIntExtra("missedCount", 0)
            Log.d("confirm missedCount", "confirm missedCount Service: $count")
            startMissedAlarmNotification(count)
            return START_NOT_STICKY
        }

        alarm =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent?.getParcelableExtra(
                "alarmData",
                AlarmData::class.java
            ) ?: AlarmData()
            else intent?.getParcelableExtra("alarmData") ?: AlarmData()

        // NotificationActivity에서 MainActivity로 넘길지에 대한 값 초기화
        preferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        editor = preferences.edit()
        editor.putBoolean("openMainActivity", false)
        editor.apply()

        val notificationIntent = createNotificationIntent(alarm)

        if (intent?.action == Const.ACTION_START_ACTIVITY) {
            startActivity(notificationIntent)
            return super.onStartCommand(intent, flags, startId)
        }

        if (alarm.flagSound) {
            Utils.initVolume(this)
            headsetCheck()
        }
        Utils.startAlarm(this, alarm)


        notificationIntent.action = Const.ACTION_NOTIFICATION
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = createNotification(alarm, pendingIntent)

        startForeground(NOTIFICATION_ID, notification)

        startActivity(notificationIntent)

        return START_REDELIVER_INTENT
    }

    private fun createNotificationIntent(alarm: AlarmData): Intent {
        return Intent(this, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("alarmData", alarm)
        }
    }

    private fun startMissedAlarmNotification(count: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = Const.ACTION_STOP_SERVICE
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = createMissedNotification(count, pendingIntent)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(
        alarm: AlarmData,
        pendingIntent: PendingIntent,
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(alarm.title)
            .setContentText(alarm.time + "알람")
            .setSmallIcon(R.drawable.ic_alarm_noti)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    private fun createMissedNotification(
        count: Int,
        pendingIntent: PendingIntent,
    ): Notification {
        Log.d("confirm missedCount", "confirm missedCount notification: $count")
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("")
            .setContentText("부재중 알람이 ${count}개 있습니다.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.ic_alarm_noti)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun headsetCheck() {
        // BroadcastReceiver 등록
        val intentFilter = IntentFilter()
        headsetReceiver.setConnectionListener(this)
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(headsetReceiver, intentFilter)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isConnected =
            audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn

        changeVolume(applicationContext, alarm.volume, isConnected)
        if (isConnected) Toast.makeText(this, "이어폰 착용으로 최대 소리가 줄어듭니다.", Toast.LENGTH_SHORT).show()
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onHeadsetConnected(isConnected: Boolean) {
        changeVolume(this, alarm.volume, isConnected)
        Toast.makeText(this, "이어폰 착용으로 최대 소리가 줄어듭니다.", Toast.LENGTH_SHORT).show()
    }
}