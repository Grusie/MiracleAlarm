package com.grusie.miraclealarm.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmTimeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarm: AlarmTimeData)

    @Delete
    suspend fun delete(alarm: AlarmTimeData)

    @Query("SELECT * FROM alarm_time_table")
    suspend fun getAllAlarmTimes(): MutableList<AlarmTimeData>

    @Query("SELECT * FROM alarm_time_table WHERE timeInMillis <= :currentTime")
    suspend fun getMissedAlarms(currentTime: Long): List<AlarmTimeData>
}