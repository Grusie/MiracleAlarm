package com.grusie.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grusie.data.model.AlarmTurnOffData

@Dao
interface AlarmTurnOffDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarmTurnOffData: AlarmTurnOffData): Long

    @Query("DELETE FROM alarm_turn_off_table WHERE alarmId =:alarmId")
    suspend fun delete(alarmId: Long)

    @Query("SELECT * FROM alarm_turn_off_table WHERE alarmId = :alarmId")
    suspend fun getOffWayById(alarmId: Long): AlarmTurnOffData?
}