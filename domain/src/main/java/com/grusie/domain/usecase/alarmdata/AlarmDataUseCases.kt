package com.grusie.domain.usecase.alarmdata

data class AlarmDataUseCases(
    val getAllAlarmListUseCases: GetAllAlarmListUseCase,
    val insertAlarmDataUseCase: InsertAlarmDataUseCase,
    val deleteAlarmDataUseCase: DeleteAlarmDataUseCase,
    val getAlarmByIdUseCase: GetAlarmByIdUseCase
)