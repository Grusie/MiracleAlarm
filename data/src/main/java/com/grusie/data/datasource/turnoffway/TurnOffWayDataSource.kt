package com.grusie.data.datasource.turnoffway

import com.grusie.data.model.AlarmTurnOffData

interface TurnOffWayDataSource {

    suspend fun insert(alarmTurnOffData: AlarmTurnOffData): Result<Long>

    suspend fun delete(alarmId: Int): Result<Unit>

    suspend fun getOffWayById(alarmId: Int): Result<AlarmTurnOffData?>
}