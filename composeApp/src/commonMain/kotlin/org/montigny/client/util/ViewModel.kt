package org.montigny.client.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel


open class ViewModel {
    // Equivalent of Android's viewModelScope
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    open fun onCleared() {
        // cancel coroutines when ViewModel is disposed
        viewModelScope.cancel()
    }
}