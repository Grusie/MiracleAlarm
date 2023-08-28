package com.grusie.miraclealarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import com.grusie.miraclealarm.interfaces.MessageUpdateListener
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.dao.AlarmTimeDao
import com.grusie.miraclealarm.util.Utils.Companion.createAlarmMessage

class TimeChangeReceiver : BroadcastReceiver() {
    private lateinit var alarmTimeDao: AlarmTimeDao
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_TIME_TICK)) {
            alarmTimeDao = AlarmDatabase.getDatabase(context).alarmTimeDao()

            alarmTimeDao.getMinAlarmTime()?.observe(context as LifecycleOwner) {
                it?.let {
                    (context as MessageUpdateListener).onMessageUpdated(
                        createAlarmMessage(false, it.timeInMillis)
                    )
                }
            }
        }
    }
}

