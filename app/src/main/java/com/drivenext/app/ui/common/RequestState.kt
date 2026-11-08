package com.drivenext.app.ui.common

sealed class RequestState {
    data object Idle : RequestState()
    data object Loading : RequestState()
    data object Success : RequestState()
    data class Error(val message: String) : RequestState()
}
