package com.grusie.domain.usecase.alarmdata

import com.grusie.domain.model.AlarmDomainModel
import com.grusie.domain.repository.AlarmDataRepository

class InsertAlarmDataUseCase(private val alarmDataRepository: AlarmDataRepository) {
    suspend operator fun invoke(alarmData: AlarmDomainModel): Result<Long> {
        return alarmDataRepository.insertAlarmData(alarmData)
    }
}