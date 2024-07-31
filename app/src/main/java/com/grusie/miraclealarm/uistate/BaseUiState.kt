package com.grusie.miraclealarm.uistate

sealed class BaseUiState {
    object Success : BaseUiState()
    object Loading : BaseUiState()
    data class Error(val description: String) : BaseUiState()
}