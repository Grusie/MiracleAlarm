package com.grusie.miraclealarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_turn_off_table")
data class AlarmTurnOffData(        //알람 끄는 방법 테이블
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var turnOffWay: String = "",
    var count: Int = 0,
    var alarmId: Int = 0
)