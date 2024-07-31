package com.grusie.miraclealarm.model.data

data class AlarmTurnOffUiModel(
    val id: Long = 0,
    var turnOffWay: String = "흔들어서 끄기",
    var count: Int = 30,
    var alarmId: Long = 0
)