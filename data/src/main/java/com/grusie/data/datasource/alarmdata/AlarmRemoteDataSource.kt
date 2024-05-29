package com.grusie.data.datasource.alarmdata

import com.grusie.data.database.AlarmDao
import com.grusie.data.model.AlarmData
import javax.inject.Inject

class AlarmRemoteDataSource @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmDataSource {
    override suspend fun getAllAlarmList(): Result<List<AlarmData>> {
        return try {
            Result.success(alarmDao.getAllAlarms())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertAlarmData(alarmData: AlarmData): Result<Long> {
        return try {
            Result.success(alarmDao.insert(alarmData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAlarmData(alarmData: AlarmData): Result<Unit> {
        return try {
            Result.success(alarmDao.delete(alarmData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAlarmById(id: Int): Result<AlarmData> {
        return try {
            Result.success(alarmDao.getAlarmById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}