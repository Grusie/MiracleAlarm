package com.example.miraclealarm

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarm : AlarmData)

    @Update
    suspend fun update(alarm: AlarmData)

    @Delete
    suspend fun delete(alarm: AlarmData)

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    fun getAlarmById(id: Int) : LiveData<AlarmData>

    @Query("SELECT * FROM alarm_table ORDER BY time ASC")
    fun getAllAlarms(): LiveData<MutableList<AlarmData>>
}