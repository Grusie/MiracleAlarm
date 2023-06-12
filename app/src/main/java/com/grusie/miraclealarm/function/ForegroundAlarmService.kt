package com.grusie.miraclealarm.function

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.activity.NotificationActivity
import com.grusie.miraclealarm.model.AlarmDao
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class ForegroundAlarmService : Service() {
    //private lateinit var wakeLock: PowerManager.WakeLock
    private var NOTIFICATION_ID by Delegates.notNull<Int>()
    private val CHANNEL_ID = "channel_id"
    private val CHANNEL_NAME = "channel_name"
    private lateinit var alarmDao: AlarmDao
    private lateinit var repository: AlarmRepository
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var alarmId : Int? = null
    private var title: String? = null
    private var content: String? = null

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
        Utils.stopAlarmSound()
        Utils.changeVolume(null)
/*        if (wakeLock.isHeld) {
            wakeLock.release()
        }*/
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alarmId = intent?.getIntExtra("alarmId", -1)
        title = intent?.getStringExtra("title")
        content = intent?.getStringExtra("contentValue")

        alarmDao = AlarmDatabase.getDatabase(applicationContext).alarmDao()
        repository = AlarmRepository(alarmDao)

        // NotificationActivity에서 MainActivity로 넘길지에 대한 값 초기화
        preferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        editor = preferences.edit()
        editor.putBoolean("openMainActivity", false)
        editor.apply()

        val notificationIntent = createNotificationIntent(alarmId, title, content)

        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

        if (intent?.action == "startActivity") {
            startActivity(notificationIntent)
            return super.onStartCommand(intent, flags, startId)
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                if (alarmId != null) {
                    val alarm = repository.getAlarmById(alarmId!!)
                    Log.d("confirm alarmData", "$alarm")

                    if (alarm.flagSound) {
                        val sound = Utils.getAlarmSound(applicationContext, alarm.sound)
                        Utils.initVolume(applicationContext)
                        Utils.changeVolume(alarm.volume)
                        Utils.playAlarmSound(applicationContext, sound)
                    }
                }
            }
        }

        NOTIFICATION_ID = System.currentTimeMillis().toInt()

        createNotificationChannel()


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
        val notification = createNotification(title, content, pendingIntent)

        startForeground(NOTIFICATION_ID, notification)
        startActivity(notificationIntent)

        return START_NOT_STICKY
    }

    private fun createNotificationIntent(alarmId: Int?, title: String?, content: String?): Intent {
        return Intent(this, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("alarmId", alarmId)
            putExtra("title", title)
            putExtra("contentValue", content)
        }
    }

    private fun createNotification(
        title: String?,
        content: String?,
        pendingIntent: PendingIntent,
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_alarm_noti)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
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
}