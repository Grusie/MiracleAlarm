package com.grusie.miraclealarm.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.grusie.miraclealarm.model.data.AlarmData

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarm: AlarmData): Long

    @Update
    suspend fun update(alarm: AlarmData)

    @Delete
    suspend fun delete(alarm: AlarmData)

    @Query("SELECT * FROM alarm_table WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmData

    @Query("SELECT * FROM alarm_table ORDER BY time ASC")
    fun getAllAlarms(): LiveData<MutableList<AlarmData>>
}