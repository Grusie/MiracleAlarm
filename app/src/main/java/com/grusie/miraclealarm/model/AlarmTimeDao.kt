package com.grusie.miraclealarm.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmTimeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarm: AlarmTimeData)

    @Delete
    suspend fun delete(alarm: AlarmTimeData)

    @Query("SELECT * FROM alarm_time_table WHERE alarmId = :alarmId")
    suspend fun getAlarmTimesByAlarmId(alarmId: Int): List<AlarmTimeData>

    @Query("DELETE FROM alarm_time_table WHERE alarmId = :alarmId")
    suspend fun deleteByAlarmId(alarmId: Int)

    @Query("SELECT * FROM alarm_time_table ORDER BY timeInMillis ASC LIMIT 1")
    fun getMinAlarmTime(): LiveData<AlarmTimeData>

    @Query("SELECT * FROM alarm_time_table WHERE timeInMillis <= :currentTime")
    suspend fun getMissedAlarms(currentTime: Long): List<AlarmTimeData>
}