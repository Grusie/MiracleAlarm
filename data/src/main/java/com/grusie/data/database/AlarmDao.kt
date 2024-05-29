package com.grusie.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grusie.data.model.AlarmData

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