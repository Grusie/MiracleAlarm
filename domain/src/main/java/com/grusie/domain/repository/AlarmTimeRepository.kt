package com.grusie.domain.repository

import com.grusie.domain.model.AlarmTimeDomainModel

interface AlarmTimeRepository {
    suspend fun insertAlarmTime(alarmTime: AlarmTimeDomainModel): Result<Long>
    suspend fun deleteAlarmTime(alarmTime: AlarmTimeDomainModel): Result<Unit>
    suspend fun getAlarmTimesByAlarmId(alarmId: Long): Result<List<AlarmTimeDomainModel>>
    suspend fun deleteByAlarmId(alarmId: Long): Result<Unit>
    suspend fun getMinAlarmTime(): Result<AlarmTimeDomainModel?>
    suspend fun getMissedAlarms(currentTime: Long): Result<List<AlarmTimeDomainModel>?>
}