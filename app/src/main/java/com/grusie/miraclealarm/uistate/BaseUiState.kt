package com.grusie.miraclealarm.uistate

sealed class BaseUiState {
    object Init: BaseUiState()
    object Loading : BaseUiState()
    object Success: BaseUiState()
    object Error: BaseUiState()
}