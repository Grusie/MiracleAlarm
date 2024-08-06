package com.grusie.miraclealarm.mapper

import com.grusie.domain.model.AlarmTurnOffDomainModel
import com.grusie.miraclealarm.model.data.AlarmTurnOffUiModel

fun AlarmTurnOffDomainModel.toUiModel(): AlarmTurnOffUiModel {
    return AlarmTurnOffUiModel(
        id = this.id,
        turnOffWay = this.turnOffWay,
        count = this.count,
        alarmId = this.alarmId
    )
}

fun AlarmTurnOffUiModel.toDomainModel(): AlarmTurnOffDomainModel {
    return AlarmTurnOffDomainModel(
        id = this.id,
        turnOffWay = this.turnOffWay,
        count = this.count,
        alarmId = this.alarmId
    )
}