package com.grusie.data.datasource.alarmtime

import com.grusie.data.model.AlarmTimeData

interface AlarmTimeDataSource {
    suspend fun insertAlarmTime(alarmTime: AlarmTimeData): Result<Long>
    suspend fun deleteAlarmTime(alarmTime: AlarmTimeData): Result<Unit>
    suspend fun getAlarmTimesByAlarmId(alarmId: Long): Result<List<AlarmTimeData>>
    suspend fun deleteByAlarmId(alarmId: Long): Result<Unit>
    suspend fun getMinAlarmTime(): Result<AlarmTimeData?>
    suspend fun getMissedAlarms(currentTime: Long): Result<List<AlarmTimeData>?>
}