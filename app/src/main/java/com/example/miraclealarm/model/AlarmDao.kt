package com.example.miraclealarm.model

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.miraclealarm.model.AlarmData

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarm : AlarmData)

    @Update
    suspend fun update(alarm: AlarmData)

    @Delete
    suspend fun delete(alarm: AlarmData)

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    suspend fun getAlarmById(id: Int) : AlarmData

    @Query("SELECT * FROM alarm_table ORDER BY time ASC")
    fun getAllAlarms(): LiveData<MutableList<AlarmData>>
}