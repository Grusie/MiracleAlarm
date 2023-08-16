package com.grusie.miraclealarm.function

import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.function.Utils.Companion.alarmManager
import com.grusie.miraclealarm.model.AlarmDao
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmRepository
import com.grusie.miraclealarm.model.AlarmTimeDao
import com.grusie.miraclealarm.model.AlarmTimeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmNotiReceiver : BroadcastReceiver() {

    private lateinit var activityManager: ActivityManager
    private lateinit var runningServices: MutableList<RunningServiceInfo>

    private val serviceName = ForegroundAlarmService::class.java.name
    private lateinit var alarmDao: AlarmDao
    private lateinit var repository: AlarmRepository
    private lateinit var alarmTimeDao: AlarmTimeDao
    private lateinit var missedAlarmList: List<AlarmTimeData>
    override fun onReceive(context: Context, intent: Intent) {
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
        alarmDao = AlarmDatabase.getDatabase(context).alarmDao()
        alarmTimeDao = AlarmDatabase.getDatabase(context).alarmTimeDao()
        repository = AlarmRepository(alarmDao)
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarm =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableExtra(
                "alarmData",
                AlarmData::class.java
            ) ?: AlarmData()
            else intent.getParcelableExtra("alarmData") ?: AlarmData()

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val allAlarms = repository.allAlarms
            Log.d("confirm contentValue2", "confirm contentValue : bootComplete")
            allAlarms.observeForever { alarmList ->
                CoroutineScope(Dispatchers.IO).launch {
                    missedAlarmList = alarmTimeDao.getMissedAlarms(System.currentTimeMillis())

                    missedAlarmList.forEach {
                        alarmTimeDao.delete(it)
                    }
                    if (missedAlarmList.isNotEmpty())
                        createMissedAlarm(context, missedAlarmList.size)

                    for (alarmData in alarmList) {
                        if (alarmData.enabled && !missedAlarmList.map { it.alarmId }
                                .contains(alarmData.id)) {
                            Utils.setAlarm(context, alarmData)
                        }
                    }
                }
            }
        } else {
            if (alarm.dateRepeat) {
                // 반복 주기: 주마다
                val intervalMillis = AlarmManager.INTERVAL_DAY * 7

                // 다음 알람 설정
                val nextAlarmTimeMillis = System.currentTimeMillis() + intervalMillis
                CoroutineScope(Dispatchers.IO).launch {
                    alarmTimeDao.insert(Utils.setAlarm(context, nextAlarmTimeMillis, alarm))
                }
            }
            val alarmTimeData = AlarmTimeData().apply {
                id = Utils.generateAlarmId(alarm, System.currentTimeMillis())
            }

            CoroutineScope(Dispatchers.IO).launch {
                alarmTimeDao.delete(alarmTimeData)
            }

            createAlarm(context, alarm)
        }
    }

    private fun createMissedAlarm(context: Context, count: Int) {
        Log.d("confirm missedCount", "confirm missedCount : $count")
        val serviceIntent = Intent(context, ForegroundAlarmService::class.java).apply {
            putExtra("missedCount", count)
            action = Const.ACTION_MISSED_ALARM
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }

    private fun createAlarm(context: Context, alarm: AlarmData) {
        val serviceIntent = Intent(context, ForegroundAlarmService::class.java).apply {
            putExtra("alarmData", alarm)
        }

        for (runningService in runningServices) {
            if (serviceName == runningService.service.className) {
                context.stopService(serviceIntent)
            }
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}