package com.grusie.miraclealarm.model.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlarmUiModel(
    val id: Long? = null,
    var title: String = "",                //알람 제목
    var time: String = "",                 //알람 시간
    var holiday: Boolean = false,          //공휴일 알람 허용 여부
    var date: String = "",                 //알람 날짜 or 요일
    var dateRepeat: Boolean = false,       //알람 요일 반복 여부
    var enabled: Boolean = true,           //알람 사용 여부
    var sound: String = "탁상시계 알람소리",      //알람 소리
    var volume: Int = 70,                   //알람 볼륨
    var vibrate: String = "Basic call",    //알람 진동
    /*var off_way: String = "흔들어서 끄기", //알람 끄는 방법*/
    var delay: String = "5분",             //미루기
    var delayCount: Int = 3,               //미루기 횟수
    var flagSound: Boolean = true,         //알람 소리 사용 여부
    var flagVibrate: Boolean = true,       //알람 진동 사용 여부
    var flagOffWay: Boolean = true,        //알람 끄는 방법 사용 여부
    var flagDelay: Boolean = true,        //알람 미루기 사용 여부
    var isChecked: Boolean = false,
) : Parcelable