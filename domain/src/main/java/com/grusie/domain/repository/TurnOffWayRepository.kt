package com.grusie.domain.repository

import com.grusie.domain.model.AlarmTurnOffDomainModel

interface TurnOffWayRepository {
    suspend fun insertTurnOffWay(alarmTurnOffData: AlarmTurnOffDomainModel): Result<Long>
    suspend fun deleteTurnOffWay(alarmId: Long): Result<Unit>
    suspend fun getOffWayById(alarmId: Long): Result<AlarmTurnOffDomainModel?>
}