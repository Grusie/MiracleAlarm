package com.grusie.data.repository

import com.grusie.data.datasource.alarmtime.AlarmTimeDataSource
import com.grusie.data.mapper.toDataModel
import com.grusie.data.mapper.toDomainModel
import com.grusie.domain.model.AlarmTimeDomainModel
import com.grusie.domain.repository.AlarmTimeRepository
import javax.inject.Inject

class AlarmTimeRepositoryImpl @Inject constructor(private val alarmTimeDataSource: AlarmTimeDataSource) :
    AlarmTimeRepository {
    override suspend fun insertAlarmTime(alarmTime: AlarmTimeDomainModel): Result<Long> {
        return alarmTimeDataSource.insertAlarmTime(alarmTime.toDataModel())
    }

    override suspend fun deleteAlarmTime(alarmTime: AlarmTimeDomainModel): Result<Unit> {
        return alarmTimeDataSource.deleteAlarmTime(alarmTime.toDataModel())
    }

    override suspend fun getAlarmTimesByAlarmId(alarmId: Long): Result<List<AlarmTimeDomainModel>> {
        return alarmTimeDataSource.getAlarmTimesByAlarmId(alarmId)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun deleteByAlarmId(alarmId: Long): Result<Unit> {
        return alarmTimeDataSource.deleteByAlarmId(alarmId)
    }

    override suspend fun getMinAlarmTime(): Result<AlarmTimeDomainModel?> {
        return alarmTimeDataSource.getMinAlarmTime().map { it?.toDomainModel() }
    }

    override suspend fun getMissedAlarms(currentTime: Long): Result<List<AlarmTimeDomainModel>?> {
        return alarmTimeDataSource.getMissedAlarms(currentTime)
            .map { list -> list?.map { it.toDomainModel() } }
    }
}