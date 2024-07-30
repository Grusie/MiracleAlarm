package com.grusie.domain.usecase.turnoff

import com.grusie.domain.model.AlarmTurnOffDomainModel
import com.grusie.domain.repository.TurnOffWayRepository

class GetOffWayByIdUseCase(private val turnOffWayRepository: TurnOffWayRepository) {
    suspend operator fun invoke(alarmId: Long): Result<AlarmTurnOffDomainModel?> {
        return turnOffWayRepository.getOffWayById(alarmId)
    }
}