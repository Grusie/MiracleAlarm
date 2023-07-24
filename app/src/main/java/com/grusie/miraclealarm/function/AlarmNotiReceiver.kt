package com.grusie.miraclealarm.function

import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.activity.NotificationActivity
import com.grusie.miraclealarm.function.Utils.Companion.alarmManager
import com.grusie.miraclealarm.function.Utils.Companion.receiverIntent
import com.grusie.miraclealarm.model.AlarmDao
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AlarmNotiReceiver : BroadcastReceiver() {

    private lateinit var activityManager: ActivityManager
    private lateinit var runningServices: MutableList<RunningServiceInfo>

    private val serviceName = ForegroundAlarmService::class.java.name
    private lateinit var alarmDao: AlarmDao
    private lateinit var repository: AlarmRepository
    var activity: NotificationActivity? = null

    override fun onReceive(context: Context, intent: Intent) {
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
        alarmDao = AlarmDatabase.getDatabase(context).alarmDao()
        repository = AlarmRepository(alarmDao)
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarm = intent.getParcelableExtra("alarmData")?: AlarmData()

        if (activity != null){
            activity?.turnOffAlarm()
        }

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val allAlarms = repository.allAlarms
            Log.d("confirm contentValue2", "confirm contentValue : bootComplete")

            allAlarms.observeForever { alarmList ->
                for (alarm in alarmList) {
                    if (alarm.enabled) {
                        createAlarm(context, alarm)
                    }
                }
            }
        } else {
            if(alarm.dateRepeat){
                // 반복 주기: 주마다
                val intervalMillis = AlarmManager.INTERVAL_DAY * 7

                // 다음 알람 설정
                val nextAlarmTimeMillis = System.currentTimeMillis() + intervalMillis

                Utils.setAlarm(context, nextAlarmTimeMillis, alarm)
            }

            createAlarm(context, alarm)
        }

    }

    private fun createAlarm(context: Context, alarm:AlarmData) {
        val serviceIntent = Intent(context, ForegroundAlarmService::class.java).apply {
            putExtra("alarmData", alarm)
        }
        for (runningService in runningServices) {
            if (serviceName == runningService.service.className) {
                Utils.stopAlarm(context)
                serviceIntent.action = "duplication"
            }
        }
        Log.d("confirm contentValue foreground", "startForeground")
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}