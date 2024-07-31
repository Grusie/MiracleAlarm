package com.grusie.data.datasource.alarmdata

import com.grusie.data.model.AlarmData

interface AlarmDataSource {

    suspend fun getAllAlarmList(): Result<List<AlarmData>>

    suspend fun insertAlarmData(alarmData: AlarmData): Result<Long>

    suspend fun deleteAlarmData(alarmData: AlarmData): Result<Unit>
    suspend fun getAlarmById(id: Long): Result<AlarmData>
}