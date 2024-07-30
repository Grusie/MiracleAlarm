package com.grusie.data.repository

import com.grusie.data.datasource.alarmdata.AlarmDataSource
import com.grusie.data.mapper.toDataModel
import com.grusie.data.mapper.toDomainModel
import com.grusie.domain.model.AlarmDomainModel
import com.grusie.domain.repository.AlarmDataRepository
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(private val alarmDataSource: AlarmDataSource) :
    AlarmDataRepository {
    override suspend fun getAllAlarmList(): Result<List<AlarmDomainModel>> {
        return alarmDataSource.getAllAlarmList().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun insertAlarmData(alarmData: AlarmDomainModel): Result<Long> {
        return alarmDataSource.insertAlarmData(alarmData.toDataModel())
    }

    override suspend fun deleteAlarmData(alarmData: AlarmDomainModel): Result<Unit> {
        return alarmDataSource.deleteAlarmData(alarmData.toDataModel())
    }

    override suspend fun getAlarmById(id: Long): Result<AlarmDomainModel> {
        return alarmDataSource.getAlarmById(id).map { it.toDomainModel() }
    }
}