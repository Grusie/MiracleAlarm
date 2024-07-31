package com.grusie.data.datasource.alarmtime

import com.grusie.data.database.AlarmTimeDao
import com.grusie.data.model.AlarmTimeData
import javax.inject.Inject

class AlarmTimeRemoteDataSource @Inject constructor(
    private val alarmTimeDao: AlarmTimeDao
) : AlarmTimeDataSource {
    override suspend fun insertAlarmTime(alarmTime: AlarmTimeData): Result<Long> {
        return try {
            Result.success(alarmTimeDao.insert(alarmTime))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAlarmTime(alarmTime: AlarmTimeData): Result<Unit> {
        return try {
            Result.success(alarmTimeDao.delete(alarmTime))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAlarmTimesByAlarmId(alarmId: Long): Result<List<AlarmTimeData>> {
        return try {
            Result.success(alarmTimeDao.getAlarmTimesByAlarmId(alarmId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteByAlarmId(alarmId: Long): Result<Unit> {
        return try {
            Result.success(alarmTimeDao.deleteByAlarmId(alarmId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMinAlarmTime(): Result<AlarmTimeData?> {
        return try {
            Result.success(alarmTimeDao.getMinAlarmTime())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMissedAlarms(currentTime: Long): Result<List<AlarmTimeData>?> {
        return try {
            Result.success(alarmTimeDao.getMissedAlarms(currentTime))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}