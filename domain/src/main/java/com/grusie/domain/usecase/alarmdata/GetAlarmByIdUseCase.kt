package com.grusie.domain.usecase.alarmdata

import com.grusie.domain.model.AlarmDataDomainModel
import com.grusie.domain.repository.AlarmDataRepository

class GetAlarmByIdUseCase(private val alarmDataRepository: AlarmDataRepository) {
    suspend operator fun invoke(id: Long): Result<AlarmDataDomainModel> {
        return alarmDataRepository.getAlarmById(id)
    }
}