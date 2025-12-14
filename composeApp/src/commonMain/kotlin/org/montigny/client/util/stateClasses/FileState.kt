package org.montigny.client.util.stateClasses

sealed class FileState {
    object Loading:FileState()
    data class Success(
       val filePath: String,
    ):FileState()
    data class Error(val message: String):FileState()
}