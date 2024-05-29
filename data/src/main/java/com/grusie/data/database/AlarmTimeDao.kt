package com.grusie.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grusie.data.model.AlarmTimeData

@Dao
interface AlarmTimeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarm: AlarmTimeData): Long

    @Delete
    suspend fun delete(alarm: AlarmTimeData)

    @Query("SELECT * FROM alarm_time_table WHERE alarmId = :alarmId")
    suspend fun getAlarmTimesByAlarmId(alarmId: Int): List<AlarmTimeData>

    @Query("DELETE FROM alarm_time_table WHERE alarmId = :alarmId")
    suspend fun deleteByAlarmId(alarmId: Int)

    @Query("SELECT * FROM alarm_time_table ORDER BY timeInMillis ASC LIMIT 1")
    fun getMinAlarmTime(): AlarmTimeData?

    @Query("SELECT * FROM alarm_time_table WHERE timeInMillis <= :currentTime")
    suspend fun getMissedAlarms(currentTime: Long): List<AlarmTimeData>?
}