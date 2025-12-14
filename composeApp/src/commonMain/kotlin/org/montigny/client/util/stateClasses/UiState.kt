package org.montigny.client.util.stateClasses

sealed class UiState  {
    object Idle:UiState()
    object Loading:UiState()
    object Success:UiState()
    data class Error(val message: String):UiState()
}