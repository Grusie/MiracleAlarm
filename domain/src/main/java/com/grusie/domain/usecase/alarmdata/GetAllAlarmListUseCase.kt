package com.grusie.domain.usecase.alarmdata

import com.grusie.domain.model.AlarmDomainModel
import com.grusie.domain.repository.AlarmDataRepository

class GetAllAlarmListUseCase(private val alarmDataRepository: AlarmDataRepository) {
    suspend operator fun invoke(): Result<List<AlarmDomainModel>> {
        return alarmDataRepository.getAllAlarmList()
    }
}