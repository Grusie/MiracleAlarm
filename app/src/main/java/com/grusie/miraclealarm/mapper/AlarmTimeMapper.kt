package com.grusie.miraclealarm.mapper

import com.grusie.domain.model.AlarmTimeDomainModel
import com.grusie.miraclealarm.model.data.AlarmTimeUiModel

fun AlarmTimeDomainModel.toUiModel(): AlarmTimeUiModel {
    return AlarmTimeUiModel(
        id = this.id,
        timeInMillis = this.timeInMillis,
        alarmId = this.alarmId
    )
}

fun AlarmTimeUiModel.toDomainModel(): AlarmTimeDomainModel {
    return AlarmTimeDomainModel(
        id = this.id,
        timeInMillis = this.timeInMillis,
        alarmId = this.alarmId
    )
}