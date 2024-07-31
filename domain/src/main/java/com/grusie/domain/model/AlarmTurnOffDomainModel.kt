package com.grusie.domain.model

data class AlarmTurnOffDomainModel(
    val id: Long,
    var turnOffWay: String,
    var count: Int,
    var alarmId: Long
)