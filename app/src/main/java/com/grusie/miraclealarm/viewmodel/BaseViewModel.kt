package com.grusie.miraclealarm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.grusie.miraclealarm.uistate.BaseEventState
import com.grusie.miraclealarm.uistate.BaseUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseViewModel : ViewModel() {
    private val _baseUiState: MutableSharedFlow<BaseUiState> = MutableStateFlow(BaseUiState.Init)
    private val _baseEventState: MutableSharedFlow<BaseEventState> = MutableSharedFlow()
    val baseUiState: SharedFlow<BaseUiState> get() = _baseUiState
    val baseEventState: SharedFlow<BaseEventState> get() = _baseEventState

    suspend fun handleError(error: Throwable) {
        Log.e(
            "${this::class.simpleName}",
            error.message ?: "",
        )
        setEventState(BaseEventState.Error(error.message ?: ""))
    }

    suspend fun setUiState(baseUiState: BaseUiState) {
        _baseUiState.emit(baseUiState)
    }

    suspend fun setEventState(baseEventState: BaseEventState) {
        _baseEventState.emit(baseEventState)
    }
}