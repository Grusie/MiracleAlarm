package com.grusie.data.datasource.turnoffway

import com.grusie.data.database.AlarmTurnOffDao
import com.grusie.data.model.AlarmTurnOffData
import javax.inject.Inject

class TurnOffWayRemoteDataSource @Inject constructor(
    private val alarmTurnOffDao: AlarmTurnOffDao
) : TurnOffWayDataSource {
    override suspend fun insertTurnOffWay(alarmTurnOffData: AlarmTurnOffData): Result<Long> {
        return try {
            Result.success(alarmTurnOffDao.insert(alarmTurnOffData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTurnOffWay(alarmId: Long): Result<Unit> {
        return try {
            Result.success(alarmTurnOffDao.delete(alarmId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOffWayById(alarmId: Long): Result<AlarmTurnOffData?> {
        return try {
            Result.success(alarmTurnOffDao.getOffWayById(alarmId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}