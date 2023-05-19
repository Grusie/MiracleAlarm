package com.example.miraclealarm.function

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class Utils {
    companion object {
        lateinit var receiverIntent: Intent
        lateinit var alarmManager: AlarmManager

        fun setAlarm(context: Context, alarmTime: Calendar, dateRepeat: Boolean, alarmId: Int) {
            receiverIntent = Intent(context, AlarmNotiReceiver::class.java)
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            Log.d("confirm utils", "${alarmTime.time}, $dateRepeat ,$alarmId 알람 설정 됨")
            receiverIntent.putExtra(
                "content",
                "${alarmTime.time} 알람"
            )

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            if (dateRepeat) {
                // 반복 주기: 주마다
                val intervalMillis = AlarmManager.INTERVAL_DAY * 7

                // 알람 설정
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    intervalMillis,
                    pendingIntent
                )

                // 알람매니저가 실행되기 전에 해당 요일로 먼저 설정
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    pendingIntent
                )
            } else {
                // 일회성 알람 설정
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    pendingIntent
                )
            }
        }

    }
}