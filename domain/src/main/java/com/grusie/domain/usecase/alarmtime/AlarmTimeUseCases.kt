package com.grusie.domain.usecase.alarmtime

data class AlarmTimeUseCases(
    val insertAlarmTime: InsertAlarmTimeUseCase,
    val deleteAlarmTimeUseCase: DeleteAlarmTimeUseCase,
    val getAlarmTimesByAlarmIdUseCase: GetAlarmTimesByAlarmIdUseCase,
    val deleteByAlarmIdUseCase: DeleteByAlarmIdUseCase,
    val getMinAlarmTimeUseCase: GetMinAlarmTimeUseCase,
    val getMissedAlarmsUseCase: GetMissedAlarmsUseCase
)