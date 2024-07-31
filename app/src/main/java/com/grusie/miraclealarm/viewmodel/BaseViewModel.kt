package com.grusie.miraclealarm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.grusie.miraclealarm.uistate.BaseUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseViewModel : ViewModel() {
    private val _baseUiState: MutableSharedFlow<BaseUiState> = MutableSharedFlow()
    val baseUiState: SharedFlow<BaseUiState> get() = _baseUiState

    suspend fun handleError(error: Throwable) {
        Log.e(
            "${this::class.simpleName}",
            error.message ?: "",
        )
        _baseUiState.emit(BaseUiState.Error(error.message ?: ""))
    }

    suspend fun setUiState(baseUiState: BaseUiState) {
        _baseUiState.emit(baseUiState)
    }
}