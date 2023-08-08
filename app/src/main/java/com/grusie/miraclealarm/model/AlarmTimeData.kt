package com.grusie.miraclealarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_time_table")    //앞으로 울릴 알람 시간 테이블
data class AlarmTimeData(
    @PrimaryKey var id:Int = 0,
    var timeInMillis:Long = 0,
    var alarmId: Int = 0
)
