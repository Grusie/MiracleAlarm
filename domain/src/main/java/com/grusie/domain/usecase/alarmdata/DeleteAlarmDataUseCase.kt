package com.grusie.domain.usecase.alarmdata

import com.grusie.domain.model.AlarmDataDomainModel
import com.grusie.domain.repository.AlarmDataRepository

class DeleteAlarmDataUseCase(private val alarmDataRepository: AlarmDataRepository) {
    suspend operator fun invoke(alarmData: AlarmDataDomainModel): Result<Unit> {
        return alarmDataRepository.deleteAlarmData(alarmData)
    }
}