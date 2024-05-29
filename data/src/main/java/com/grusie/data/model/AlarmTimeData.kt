package com.grusie.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_time_table")    //앞으로 울릴 알람 시간 테이블
data class AlarmTimeData(
    @PrimaryKey
    val id: Int,
    val timeInMillis: Long,
    val alarmId: Int
)
