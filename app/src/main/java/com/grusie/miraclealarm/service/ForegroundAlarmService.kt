package com.grusie.miraclealarm.service

import android.Manifest
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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.activity.MainActivity
import com.grusie.miraclealarm.activity.NotificationActivity
import com.grusie.miraclealarm.interfaces.HeadsetConnectionListener
import com.grusie.miraclealarm.mapper.toData
import com.grusie.miraclealarm.model.data.AlarmData
import com.grusie.miraclealarm.receiver.HeadsetReceiver
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.util.Utils.Companion.changeVolume
import com.grusie.miraclealarm.util.Utils.Companion.checkPermission
import com.grusie.miraclealarm.util.Utils.Companion.getAlarmData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

class ForegroundAlarmService : Service(), HeadsetConnectionListener,
    CoroutineScope {
    private var notificationId by Delegates.notNull<Int>()
    private val channelId = "channelId"
    private val channelName = "channelName"
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var headsetReceiver = HeadsetReceiver()
    private lateinit var alarm: AlarmData
    private var isConnected = false
    private var initVolume = 0

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(headsetReceiver)
        } catch (e: Exception) {
        }
        Utils.stopAlarmSound(this)
        changeVolume(this, null, isConnected)
        job.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        notificationId = System.currentTimeMillis().toInt()

        //부재중 알람 처리
        if (intent?.action == Const.ACTION_MISSED_ALARM) {
            val count = intent.getIntExtra("missedCount", 0)
            startMissedAlarmNotification(count)
            return START_NOT_STICKY
        }

        alarm = getAlarmData(intent).toData()

        // NotificationActivity에서 MainActivity로 넘길지에 대한 값 초기화
        preferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        editor = preferences.edit()
        editor.putBoolean("openMainActivity", false)
        editor.apply()

        val notificationIntent = createNotificationIntent(alarm)

        //notificationActivity를 다시 실행 시킬 때, 처리
        if (intent?.action == Const.ACTION_START_ACTIVITY) {
            startActivity(notificationIntent)
            return super.onStartCommand(intent, flags, startId)
        }

        if (alarm.flagSound) {
            initVolume = Utils.initVolume(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (checkPermission(this, Manifest.permission.BLUETOOTH_CONNECT))
                    headsetCheck()
            } else {
                if (checkPermission(this, Manifest.permission.BLUETOOTH))
                    headsetCheck()
            }
            changeVolumeEternally(alarm)
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

        startForeground(notificationId, notification)

        startActivity(notificationIntent)

        return START_REDELIVER_INTENT
    }


    /**
     * 소리 강제로 키우기(10초 간격)
     **/
    private fun changeVolumeEternally(alarm: AlarmData) {
        launch {
            while (isActive) {
                delay(10000)
                if (alarm.volume > Utils.getCurrentVolume(this@ForegroundAlarmService))
                    changeVolume(this@ForegroundAlarmService, alarm.volume, isConnected)
            }
        }
    }

    /**
     * 노티피케이션 액티비티 실행
     **/
    private fun createNotificationIntent(alarm: AlarmData): Intent {
        return Intent(this, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("alarmData", alarm)
        }
    }


    /**
     * 부재중알람 노티피케이션 실행
     **/
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
        startForeground(notificationId, notification)
    }


    /**
     * 노티피케이션 생성
     **/
    private fun createNotification(
        alarm: AlarmData,
        pendingIntent: PendingIntent,
    ): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(alarm.title)
            .setContentText(alarm.time + "알람")
            .setSmallIcon(R.drawable.ic_alarm_noti)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }


    /**
     * 부재중 알람 노티피케이션 생성
     **/
    private fun createMissedNotification(
        count: Int,
        pendingIntent: PendingIntent,
    ): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("")
            .setContentText(getString(R.string.str_missed_alarm, count))
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
        if (isConnected) Toast.makeText(this, R.string.str_headset_connected, Toast.LENGTH_SHORT)
            .show()
    }


    /**
     * notification 채널 생성
     **/
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            channelName,
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
        Toast.makeText(this, getString(R.string.str_headset_connected), Toast.LENGTH_SHORT).show()
    }

}