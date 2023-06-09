package com.grusie.miraclealarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "alarm_table")
data class AlarmData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String = "",                //알람 제목
    var time: String = "",                 //알람 시간
    var holiday: Boolean = false,          //공휴일 알람 허용 여부
    var date: String = "",                 //알람 날짜 or 요일
    var dateRepeat: Boolean = false,       //알람 요일 반복 여부
    var enabled: Boolean = true,           //알람 사용 여부
    var sound: String = "탁상시계 알람소리",      //알람 소리
    var volume: Int = 1,                   //알람 볼륨
    var vibrate: String = "Basic call",    //알람 진동
    var off_way: String = "흔들어서 끄기", //알람 끄는 방법
    var repeat: String = "5분,3회",        //반복 알람
    var flagSound: Boolean = true,         //알람 소리 사용 여부
    var flagVibrate: Boolean = true,       //알람 진동 사용 여부
    var flagOffWay: Boolean = true,        //알람 끄는 방법 사용 여부
    var flagRepeat: Boolean = false,       //알람 반복 사용 여부
)
