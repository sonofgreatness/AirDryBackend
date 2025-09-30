package org.montigny.client.ui.users.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.util.Constants.Companion.FAILED_LOGIN
import org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger

class AuthViewModel(
    private val sessionManager: SessionManager,
                    private  val userApi: UserApi,
                    private val logger: Logger
) : ViewModel()  {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

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



    @OptIn(ExperimentalSerializationApi::class)

    fun login() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                logger.debug("Attempting login for $username")
                userApi.login(username, password) // may throw MissingFieldException

                _uiState.value = UiState.Success

                logger.debug("Login successful for $username")
            } catch (e: kotlinx.serialization.MissingFieldException) {
                logger.warn("Invalid credentials for $username", e)
                _uiState.value = UiState.Error("Invalid username or password")
            } catch (e: io.ktor.serialization.JsonConvertException) {
                val msg = e.message ?: "Serialization error"
                logger.error("Login failed ❌ $msg", e)
                _uiState.value = UiState.Error(FAILED_LOGIN)
            } catch (e: Exception) {
                val msg = e.message ?: "Unexpected error"
                logger.error("Login failed ❌ $msg", e)
                _uiState.value = UiState.Error(FAILED_LOGIN)
            }
        }
    }

}