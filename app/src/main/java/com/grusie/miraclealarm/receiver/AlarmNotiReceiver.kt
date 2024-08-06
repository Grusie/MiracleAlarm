package com.grusie.miraclealarm.receiver

import android.app.ActivityManager
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.mapper.toData
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmRepository
import com.grusie.miraclealarm.model.dao.AlarmDao
import com.grusie.miraclealarm.model.dao.AlarmTimeDao
import com.grusie.miraclealarm.model.data.AlarmData
import com.grusie.miraclealarm.model.data.AlarmTimeData
import com.grusie.miraclealarm.service.ForegroundAlarmService
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.util.Utils.Companion.alarmManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmNotiReceiver : BroadcastReceiver() {

    private lateinit var activityManager: ActivityManager
    private lateinit var runningServices: List<ActivityManager.RunningAppProcessInfo>

    private val serviceName = ForegroundAlarmService::class.java.name
    private lateinit var alarmDao: AlarmDao
    private lateinit var repository: AlarmRepository
    private lateinit var alarmTimeDao: AlarmTimeDao
    private lateinit var missedAlarmList: List<AlarmTimeData>
    private lateinit var alarm: AlarmData
    override fun onReceive(context: Context, intent: Intent) {
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        runningServices = activityManager.runningAppProcesses
        alarmDao = AlarmDatabase.getDatabase(context).alarmDao()
        alarmTimeDao = AlarmDatabase.getDatabase(context).alarmTimeDao()
        repository = AlarmRepository(alarmDao)
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarm = Utils.getAlarmData(intent).toData()

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            bootComplete(context)
        } else {
            receiveAlarm(context)
        }
    }


    /**
     * 알람 데이터 받았을 때 처리
     **/
    private fun receiveAlarm(context: Context) {
        if (alarm.dateRepeat) {
            // 반복 주기: 주마다
            val intervalMillis = AlarmManager.INTERVAL_DAY * 7

            // 다음 알람 설정
            val nextAlarmTimeMillis = System.currentTimeMillis() + intervalMillis
            CoroutineScope(Dispatchers.IO).launch {
                alarmTimeDao.insert(
                    Utils.setAlarm(context, nextAlarmTimeMillis, alarm.toUiModel()).toData()
                )
            }
        }
        val alarmTimeData = AlarmTimeData().apply {
            id = Utils.generateAlarmId(alarm.toUiModel(), System.currentTimeMillis()).toInt()
        }

        CoroutineScope(Dispatchers.IO).launch {
            alarmTimeDao.delete(alarmTimeData)
        }

        createAlarm(context, alarm)
    }


    /**
     * 부팅이 완료되었을 때의 처리
     **/
    private fun bootComplete(context: Context) {
        val allAlarms = repository.allAlarms
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
                        Utils.setAlarm(context, alarmData.toUiModel())
                    }
                }
            }
        }
    }


    /**
     * 부재중 알람 생성
     **/
    private fun createMissedAlarm(context: Context, count: Int) {
        val serviceIntent = Intent(context, ForegroundAlarmService::class.java).apply {
            putExtra("missedCount", count)
            action = Const.ACTION_MISSED_ALARM
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }


    /**
     * 알람 생성
     **/
    private fun createAlarm(context: Context, alarm: AlarmData) {
        val serviceIntent = Intent(context, ForegroundAlarmService::class.java).apply {
            putExtra("alarmData", alarm)
        }

        for (processInfo in runningServices) {
            if (serviceName == processInfo.processName) {
                context.stopService(serviceIntent)

                break
            }
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}