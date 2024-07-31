package com.grusie.domain.repository

import com.grusie.domain.model.AlarmDomainModel

interface AlarmDataRepository {
    suspend fun getAllAlarmList(): Result<List<AlarmDomainModel>>
    suspend fun insertAlarmData(alarmData: AlarmDomainModel): Result<Long>
    suspend fun deleteAlarmData(alarmData: AlarmDomainModel): Result<Unit>
    suspend fun getAlarmById(id: Long): Result<AlarmDomainModel>
}