package com.grusie.miraclealarm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmTurnOffDao

class AlarmTurnOffViewModel(application: Application) : AndroidViewModel(application) {
    private val alarmTurnOffDao: AlarmTurnOffDao

    init {
        alarmTurnOffDao = AlarmDatabase.getDatabase(application).alarmTurnOffDao()
    }
    suspend fun getOffWayById(alarm: AlarmData){
        alarmTurnOffDao.getOffWayById(alarm.id)
    }
}