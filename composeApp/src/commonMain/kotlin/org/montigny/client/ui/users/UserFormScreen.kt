package org.montigny.client.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.util.stateClasses.UiState
import org.montigny.client.util.stateClasses.UserRegisterDTOState

@Composable
fun UserFormScreen (userViewModel: UserViewModel){

    val createUser by userViewModel.createUser.collectAsState()
    val uiState by userViewModel.createUserState.collectAsState()
    LaunchedEffect(uiState,) {
        userViewModel.initializeEmptyCreateUse()
    }
    Column(Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        OutlinedTextField(
            value =userViewModel.createUser.value!!.name,
            onValueChange = { userViewModel.onCreateUserChange(it,
                UserRegisterDTOState.name) },
            label = { Text("Name") }
        )

        OutlinedTextField(
            value = createUser?.surname.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it,
                UserRegisterDTOState.surname) },
            label = { Text("Surname") }
        )
        OutlinedTextField(
            value = createUser?.phoneNumber.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it,
                UserRegisterDTOState.phoneNumber) },
            label = { Text("Phone Number") }
        )
        OutlinedTextField(
            value = createUser?.secondaryPhoneNumber.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it,
                UserRegisterDTOState.secondaryPhoneNumber) },
            label = { Text("Secondary Phone Number") }
        )
        OutlinedTextField(
            value = createUser?.email.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it,
                UserRegisterDTOState.email) },
            label = { Text("Email") }
        )


        PasswordField(
            password = createUser?.password.orEmpty(),
            onPasswordChange = {
                userViewModel.onCreateUserChange(it,
                    UserRegisterDTOState.password)
            },
            onDone = { userViewModel.createUser() }
        )


        Button(
            onClick = {userViewModel.createUser()},
            enabled = uiState !is UiState.Loading
        ) {
            Text(if (uiState is UiState.Loading) "Creating..." else "Create")
        }

        when (uiState) {
            is UiState.Error -> Text(
                (uiState as UiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            is UiState.Success -> Text(
                "✅ created  successfully!",
                color = MaterialTheme.colorScheme.primary
            )
            else -> {}
        }
    }

 }
