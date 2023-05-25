package com.example.miraclealarm.function

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.miraclealarm.model.AlarmData
import java.util.*

class Utils {
    companion object {
        lateinit var receiverIntent: Intent
        lateinit var alarmManager: AlarmManager

        fun setAlarm(context: Context, alarmTime: Calendar, alarm: AlarmData) {
            val dateRepeat = alarm.dateRepeat
            val alarmId = alarm.id
            val alarmTitle = alarm.title
            val timeMillis = alarmTime.timeInMillis

            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Log.d("confirm contentValue", "${alarmTime.time}, $dateRepeat ,$alarmId 알람 설정 됨")
            receiverIntent.putExtra(
                "content", "${alarm.time} 알람"
            )
            receiverIntent.putExtra("title", alarmTitle)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmTime.timeInMillis, pendingIntent)

            if (dateRepeat) {
                // 반복 주기: 주마다
                val intervalMillis = AlarmManager.INTERVAL_DAY * 7

                // 알람 설정
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, timeMillis, intervalMillis, pendingIntent
                )

                // 알람매니저가 실행되기 전에 해당 요일로 먼저 설정
                alarmManager.setAlarmClock(
                    alarmClockInfo, pendingIntent
                )
            } else {
                // 일회성 알람 설정
                alarmManager.setAlarmClock(
                    alarmClockInfo, pendingIntent
                )
            }
        }

        fun delAlarm(context: Context, alarm: AlarmData) {
            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,
                receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)

            Log.d("confirm contentValue", "${alarm.id} 알람 제거 됨")
        }

    }
}