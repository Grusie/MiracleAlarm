package com.grusie.miraclealarm.model

import androidx.lifecycle.LiveData
import com.grusie.miraclealarm.model.dao.AlarmDao
import com.grusie.miraclealarm.model.data.AlarmData

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: LiveData<MutableList<AlarmData>> = alarmDao.getAllAlarms()

    suspend fun insert(alarm: AlarmData): Long {
        return alarmDao.insert(alarm)
    }

    suspend fun update(alarm: AlarmData) {
        alarmDao.update(alarm)
    }

    suspend fun delete(alarm: AlarmData) {
        alarmDao.delete(alarm)
    }

    suspend fun getAlarmById(id: Int): AlarmData {
        return alarmDao.getAlarmById(id)
    }
}