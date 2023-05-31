package com.grusie.miraclealarm.function

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmRepository

class AlarmNotiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var title = ""
        var contentValue = ""
        var alarmId: Int

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmDao = AlarmDatabase.getDatabase(context).alarmDao()
            val repository = AlarmRepository(alarmDao)
            val allAlarms = repository.allAlarms

            Log.d("confirm contentValue", "confirm contentValue : bootComplete")

            allAlarms.observeForever { alarmList ->
                for (alarm in alarmList) {
                    if (alarm.enabled) {
                        title = alarm.title
                        contentValue = alarm.time + " 알람"
                        alarmId = alarm.id

                        Log.d("confirm contentValue", "confirm contentValue : $contentValue 생성됨")
                        createAlarm(context, alarmId, title, contentValue)
                    }
                }
            }
        } else {
            alarmId = intent.getIntExtra("alarmId", -1)
            title = intent.getStringExtra("title").toString()
            contentValue = intent.getStringExtra("contentValue").toString()

            Log.d("confirm contentValue", "confirm contentValue : $contentValue")
            createAlarm(context, alarmId, title, contentValue)
        }

    }

    private fun createAlarm(context: Context, alarmId: Int, title: String, contentValue: String) {

        val serviceIntent = Intent(context, ForegroundAlarmService::class.java).apply {
            action = context.getString(R.string.ACTION_DISMISS)
            putExtra("alarmId", alarmId)
            putExtra("title", title)
            putExtra("contentValue", contentValue)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}