package com.grusie.data.datasource.turnoffway

import com.grusie.data.model.AlarmTurnOffData

interface TurnOffWayDataSource {

    suspend fun insertTurnOffWay(alarmTurnOffData: AlarmTurnOffData): Result<Long>

    suspend fun deleteTurnOffWay(alarmId: Long): Result<Unit>

    suspend fun getOffWayById(alarmId: Long): Result<AlarmTurnOffData?>
}