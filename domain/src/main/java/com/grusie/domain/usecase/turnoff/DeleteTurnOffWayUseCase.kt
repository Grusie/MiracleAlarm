package com.grusie.domain.usecase.turnoff

import com.grusie.domain.repository.TurnOffWayRepository

class DeleteTurnOffWayUseCase(private val turnOffWayRepository: TurnOffWayRepository) {
    suspend operator fun invoke(alarmId: Long): Result<Unit> {
        return turnOffWayRepository.deleteTurnOffWay(alarmId)
    }
}