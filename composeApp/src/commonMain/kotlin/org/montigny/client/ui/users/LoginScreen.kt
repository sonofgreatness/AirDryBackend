package org.montigny.client.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.montigny.client.auth.SessionManager
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.util.stateClasses.UiState
@Composable
fun LoginScreen(authViewModel: AuthViewModel, sessionManager: SessionManager, onLoginSuccess: () -> Unit) {
    val uiState by authViewModel.uiState.collectAsState()
    val isLoggedIn by sessionManager.isLoggedInFlow.collectAsState()
    var handledSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(uiState, isLoggedIn) {
        if (!handledSuccess && uiState is UiState.Success && isLoggedIn) {
            handledSuccess = true
            onLoginSuccess()
            authViewModel.resetState()
        }
    }

    Column(Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        OutlinedTextField(
            value = authViewModel.username,
            onValueChange = { authViewModel.onUsernameChange(it) },
            label = { Text("Username") }
        )
        PasswordField(
            password = authViewModel.password,
            onPasswordChange = { authViewModel.onPasswordChange(it) },
            onDone = { authViewModel.login() } // trigger login on IME "Done"
        )


        Button(
            onClick = { authViewModel.login() },
            enabled = uiState !is UiState.Loading
        ) {
            Text(if (uiState is UiState.Loading) "Logging in..." else "Login")
        }

        when (uiState) {
            is UiState.Error -> Text(
                (uiState as UiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            is UiState.Success -> Text(
                "✅ Logged in successfully!",
                color = MaterialTheme.colorScheme.primary
            )
            else -> {}
        }
    }
}
