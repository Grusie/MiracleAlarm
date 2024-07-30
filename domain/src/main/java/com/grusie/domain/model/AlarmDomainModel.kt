package com.grusie.domain.model

data class AlarmDomainModel(
    val id: Long?,
    val title: String,              //알람 제목
    val time: String,               //알람 시간
    val holiday: Boolean,           //공휴일 알람 허용 여부
    val date: String,               //알람 날짜 or 요일
    val dateRepeat: Boolean,        //알람 요일 반복 여부
    val enabled: Boolean,           //알람 사용 여부
    val sound: String,              //알람 소리
    val volume: Int,                //알람 볼륨
    val vibrate: String,            //알람 진동
    val delay: String,              //미루기
    val delayCount: Int,            //미루기 횟수
    val flagSound: Boolean,         //알람 소리 사용 여부
    val flagVibrate: Boolean,       //알람 진동 사용 여부
    val flagOffWay: Boolean,        //알람 끄는 방법 사용 여부
    val flagDelay: Boolean,        //알람 미루기 사용 여부
)