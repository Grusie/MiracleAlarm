package com.example.miraclealarm

import androidx.lifecycle.LiveData

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: LiveData<MutableList<AlarmData>> = alarmDao.getAllAlarms()

    suspend fun insert(alarm: AlarmData){
        alarmDao.insert(alarm)
    }

    suspend fun update(alarm: AlarmData){
        alarmDao.update(alarm)
    }

    suspend fun delete(alarm: AlarmData){
        alarmDao.delete(alarm)
    }
}