package org.montigny.client.ui.users.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.Constants.Companion.FAILED_LOGIN
import org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.ApiResponse
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger

class AuthViewModel(
    private  val userApi: UserApi,
                    private val logger: Logger,
    private val userRepository: UserRepository,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel()  {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable

    // move form fields here
    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun onUsernameChange(newValue: String) {
        username = newValue
    }
    fun onPasswordChange(newValue: String) {
        password = newValue
    }
    fun clearFields() {
        username = ""
        password = ""
    }
    fun resetState() {
        _uiState.value = UiState.Idle
    }

    fun login()
    {
        loginInternal(isNetworkAvailable.value)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loginInternal(isNetworkAvailable: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                if (!isNetworkAvailable) {
                val response = userRepository.offlineLogin(username, password)
                when (response) {
                    is ApiResponse.Created -> {
                        _uiState.value = UiState.Success
                    }
                    is ApiResponse.Error -> {
                        logger.warn("Offline login failed ❌ code=${response.code} msg=${response.message}")
                        _uiState.value = UiState.Error("Offline login failed: ${response.message}")
                    }

                    else -> {_uiState.value = UiState.Success}
                }
                } else {
                   if ( userApi.login(username, password))
                    _uiState.value = UiState.Success
                    else
                       _uiState.value = UiState.Error(FAILED_LOGIN)
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Unexpected error"
                logger.error("Login failed ❌ $msg", e)
                _uiState.value = UiState.Error(FAILED_LOGIN)
            }
        }
    }



    enum class LoginMode { ONLINE, OFFLINE }

    data class AuthResult(
        val mode: LoginMode,
        val success: Boolean,
        val message: String? = null
    )


}