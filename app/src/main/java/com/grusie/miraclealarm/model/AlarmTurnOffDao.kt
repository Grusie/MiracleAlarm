package com.grusie.miraclealarm.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlarmTurnOffDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarmTurnOffData: AlarmTurnOffData)

    @Update
    suspend fun update(alarmTurnOffData: AlarmTurnOffData)

    @Delete
    suspend fun delete(alarmTurnOffData: AlarmTurnOffData)

    @Query("SELECT * FROM alarm_turn_off_table WHERE alarmId = :alarmId")
    suspend fun getOffWayById(alarmId: Int) : AlarmTurnOffData
}