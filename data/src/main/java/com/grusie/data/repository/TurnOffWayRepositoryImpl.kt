package com.grusie.data.repository

import com.grusie.data.datasource.turnoffway.TurnOffWayDataSource
import com.grusie.data.mapper.toDataModel
import com.grusie.data.mapper.toDomainModel
import com.grusie.domain.model.AlarmTurnOffDomainModel
import com.grusie.domain.repository.TurnOffWayRepository
import javax.inject.Inject

class TurnOffWayRepositoryImpl @Inject constructor(private val turnOffWayDataSource: TurnOffWayDataSource) :
    TurnOffWayRepository {
    override suspend fun insertTurnOffWay(alarmTurnOffData: AlarmTurnOffDomainModel): Result<Long> {
        return turnOffWayDataSource.insertTurnOffWay(alarmTurnOffData.toDataModel())
    }

    override suspend fun deleteTurnOffWay(alarmId: Long): Result<Unit> {
        return turnOffWayDataSource.deleteTurnOffWay(alarmId)
    }

    override suspend fun getOffWayById(alarmId: Long): Result<AlarmTurnOffDomainModel?> {
        return turnOffWayDataSource.getOffWayById(alarmId).map { it?.toDomainModel() }
    }
}