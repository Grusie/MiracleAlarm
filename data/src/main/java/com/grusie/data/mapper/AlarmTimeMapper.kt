package com.grusie.data.mapper

import com.grusie.data.model.AlarmTimeData
import com.grusie.domain.model.AlarmTimeDomainModel

fun AlarmTimeData.toDomainModel(): AlarmTimeDomainModel {
    return AlarmTimeDomainModel(
        id = this.id,
        timeInMillis = this.timeInMillis,
        alarmId = this.alarmId
    )
}

fun AlarmTimeDomainModel.toDataModel(): AlarmTimeData {
    return AlarmTimeData(
        id = this.id,
        timeInMillis = this.timeInMillis,
        alarmId = this.alarmId
    )
}