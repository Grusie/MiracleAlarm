package com.grusie.domain.repository

import com.grusie.domain.model.AlarmDataDomainModel

interface AlarmDataRepository {
    suspend fun getAllAlarmList(): Result<List<AlarmDataDomainModel>>
    suspend fun insertAlarmData(alarmData: AlarmDataDomainModel): Result<Long>
    suspend fun deleteAlarmData(alarmData: AlarmDataDomainModel): Result<Unit>
    suspend fun getAlarmById(id: Long): Result<AlarmDataDomainModel>
}