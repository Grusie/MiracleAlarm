package com.grusie.miraclealarm.mapper

import com.grusie.domain.model.AlarmDomainModel
import com.grusie.miraclealarm.model.data.AlarmUiModel

fun AlarmDomainModel.toUiModel(): AlarmUiModel {
    return AlarmUiModel(
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

fun AlarmUiModel.toDomainModel(): AlarmDomainModel {
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