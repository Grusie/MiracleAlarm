package com.grusie.miraclealarm.interfaces

import com.grusie.miraclealarm.model.data.AlarmUiModel

interface AlarmListClickListener {
    fun alarmOnClickListener(alarmUiModel: AlarmUiModel)
    fun alarmOnLongClickListener(alarmUiModel: AlarmUiModel)
    fun changeAlarmEnable(alarmUiModel: AlarmUiModel)
}