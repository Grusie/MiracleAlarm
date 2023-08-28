package com.grusie.miraclealarm.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grusie.miraclealarm.model.data.AlarmTurnOffData

@Dao
interface AlarmTurnOffDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarmTurnOffData: AlarmTurnOffData)

    @Query("DELETE FROM alarm_turn_off_table WHERE alarmId =:alarmId")
    suspend fun delete(alarmId: Int)

    @Query("SELECT * FROM alarm_turn_off_table WHERE alarmId = :alarmId")
    suspend fun getOffWayById(alarmId: Int): AlarmTurnOffData?
}