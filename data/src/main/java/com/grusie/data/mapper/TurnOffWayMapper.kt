package com.grusie.data.mapper

import com.grusie.data.model.AlarmTurnOffData
import com.grusie.domain.model.AlarmTurnOffDomainModel

fun AlarmTurnOffData.toDomainModel(): AlarmTurnOffDomainModel {
    return AlarmTurnOffDomainModel(
        id = this.id,
        turnOffWay = this.turnOffWay,
        count = this.count,
        alarmId = this.alarmId
    )
}

fun AlarmTurnOffDomainModel.toDataModel(): AlarmTurnOffData {
    return AlarmTurnOffData(
        id = this.id,
        turnOffWay = this.turnOffWay,
        count = this.count,
        alarmId = this.alarmId
    )
}