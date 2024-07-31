package com.grusie.data.mapper

import com.grusie.data.model.AlarmData
import com.grusie.domain.model.AlarmDomainModel

fun AlarmData.toDomainModel(): AlarmDomainModel {
    return AlarmDomainModel(
        id = this.id,
        title = this.title,
        time = this.time,
        holiday = this.holiday,
        date = this.date,
        dateRepeat = this.dateRepeat,
        enabled = this.enabled,
        sound = this.sound,
        volume = this.volume,
        vibrate = this.vibrate,
        delay = this.delay,
        delayCount = this.delayCount,
        flagSound = this.flagSound,
        flagVibrate = this.flagVibrate,
        flagOffWay = this.flagOffWay,
        flagDelay = this.flagDelay
    )
}

fun AlarmDomainModel.toDataModel(): AlarmData {
    return AlarmData(
        id = this.id,
        title = this.title,
        time = this.time,
        holiday = this.holiday,
        date = this.date,
        dateRepeat = this.dateRepeat,
        enabled = this.enabled,
        sound = this.sound,
        volume = this.volume,
        vibrate = this.vibrate,
        delay = this.delay,
        delayCount = this.delayCount,
        flagSound = this.flagSound,
        flagVibrate = this.flagVibrate,
        flagOffWay = this.flagOffWay,
        flagDelay = this.flagDelay
    )
}