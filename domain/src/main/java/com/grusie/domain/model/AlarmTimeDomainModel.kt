package com.grusie.domain.model

data class AlarmTimeDomainModel(
    val id: Long,
    val timeInMillis: Long,
    val alarmId: Long
)