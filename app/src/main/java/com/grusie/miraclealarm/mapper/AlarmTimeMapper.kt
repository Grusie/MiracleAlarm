package com.grusie.miraclealarm.mapper

import com.grusie.domain.model.AlarmTimeDomainModel
import com.grusie.miraclealarm.model.data.AlarmTimeData
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

fun AlarmTimeData.toUiModel(): AlarmTimeUiModel {
    return AlarmTimeUiModel(
        id = this.id.toLong(),
        timeInMillis = this.timeInMillis,
        alarmId = this.alarmId.toLong()
    )
}

fun AlarmTimeUiModel.toData(): AlarmTimeData {
    return AlarmTimeData(
        id = this.id.toInt(),
        timeInMillis = this.timeInMillis,
        alarmId = this.alarmId.toInt()
    )
}