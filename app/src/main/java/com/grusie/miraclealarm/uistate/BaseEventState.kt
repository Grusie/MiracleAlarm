package com.grusie.miraclealarm.uistate

sealed class BaseEventState {
    data class Alert(val msgType: Int) : BaseEventState()
    data class Error(val description: String) : BaseEventState()
}