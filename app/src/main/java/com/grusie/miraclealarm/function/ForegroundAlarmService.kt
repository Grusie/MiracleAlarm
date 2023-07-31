package com.grusie.miraclealarm.function

import android.app.*
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.activity.NotificationActivity
import com.grusie.miraclealarm.function.Utils.Companion.handleHeadsetConnection
import com.grusie.miraclealarm.model.AlarmData
import kotlin.properties.Delegates

class ForegroundAlarmService : Service(), HeadsetReceiver.HeadsetConnectionListener {
    //private lateinit var wakeLock: PowerManager.WakeLock
    private var NOTIFICATION_ID by Delegates.notNull<Int>()
    private val CHANNEL_ID = "channel_id"
    private val CHANNEL_NAME = "channel_name"
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var headsetReceiver = HeadsetReceiver()
    private lateinit var alarm: AlarmData

    override fun onCreate() {
        super.onCreate()

/*        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE, "app:com.grusie.miraclealarm"
        )

        wakeLock.acquire()*/
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(headsetReceiver)
        } catch (e: Exception) {
        }
        Utils.stopAlarmSound(this)
        Utils.changeVolume(this, null)
/*        if (wakeLock.isHeld) {
            wakeLock.release()
        }*/
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alarm = intent?.getParcelableExtra("alarmData") ?: AlarmData()

        // NotificationActivity에서 MainActivity로 넘길지에 대한 값 초기화
        preferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        editor = preferences.edit()
        editor.putBoolean("openMainActivity", false)
        editor.apply()

        val notificationIntent = createNotificationIntent(alarm)

        if (intent?.action == "startActivity") {
            startActivity(notificationIntent)
            return super.onStartCommand(intent, flags, startId)
        }


        if (alarm.flagSound) {
            val sound = Utils.getAlarmSound(this, alarm.sound)
            Utils.initVolume(this)
            headsetCheck()
            Utils.changeVolume(this, alarm.volume)
            Utils.playAlarmSound(this, sound)
        }

        NOTIFICATION_ID = System.currentTimeMillis().toInt()

        createNotificationChannel()


        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = createNotification(alarm, pendingIntent)

        startForeground(NOTIFICATION_ID, notification)

        startActivity(notificationIntent)

        return START_NOT_STICKY
    }

    private fun createNotificationIntent(alarm: AlarmData): Intent {
        return Intent(this, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("alarmData", alarm)
        }
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

    private fun headsetCheck() {
        // BroadcastReceiver 등록
        val intentFilter = IntentFilter()
        headsetReceiver.setConnectionListener(this)
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(headsetReceiver, intentFilter)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isConnected =
            audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn

        handleHeadsetConnection(applicationContext, isConnected, alarm.volume)
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onHeadsetConnected(isConnected: Boolean) {
        handleHeadsetConnection(this, isConnected, alarm.volume)
    }
}