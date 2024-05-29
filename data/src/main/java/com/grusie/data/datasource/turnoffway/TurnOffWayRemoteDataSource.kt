package com.grusie.data.datasource.turnoffway

import com.grusie.data.database.AlarmTurnOffDao
import com.grusie.data.model.AlarmTurnOffData
import javax.inject.Inject

class TurnOffWayRemoteDataSource @Inject constructor(
    private val alarmTurnOffDao: AlarmTurnOffDao
) : TurnOffWayDataSource {
    override suspend fun insert(alarmTurnOffData: AlarmTurnOffData): Result<Long> {
        return try {
            Result.success(alarmTurnOffDao.insert(alarmTurnOffData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(alarmId: Int): Result<Unit> {
        return try {
            Result.success(alarmTurnOffDao.delete(alarmId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOffWayById(alarmId: Int): Result<AlarmTurnOffData?> {
        return try {
            Result.success(alarmTurnOffDao.getOffWayById(alarmId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}