package com.grusie.domain.usecase.alarmdata

import com.grusie.domain.model.AlarmDataDomainModel
import com.grusie.domain.repository.AlarmDataRepository

class GetAllAlarmListUseCase(private val alarmDataRepository: AlarmDataRepository) {
    suspend operator fun invoke(): Result<List<AlarmDataDomainModel>> {
        return alarmDataRepository.getAllAlarmList()
    }
}