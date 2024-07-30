package com.grusie.domain.usecase.turnoff

data class TurnOffWayUseCases(
    val insertTurnOffWay: InsertTurnOffWayUseCase,
    val deleteTurnOffWay: DeleteTurnOffWayUseCase,
    val getOffWayByIdUseCase: GetOffWayByIdUseCase
)