package org.montigny.client.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.util.stateClasses.UiState
import org.montigny.client.util.stateClasses.UserRegisterDTOState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(userViewModel: UserViewModel) {
    val createUser by userViewModel.createUser.collectAsState()
    val uiState by userViewModel.createUserState.collectAsState()
    val userTypes by userViewModel.useTypes.collectAsState()

    // Currently selected UserType
    var expanded by remember { mutableStateOf(false) }
    val selectedUserType = userTypes.find { it.id == createUser?.userTypeId?.toString() }

    LaunchedEffect(Unit) {
        userViewModel.initializeEmptyCreateUse()
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Existing fields ...
        OutlinedTextField(
            value = createUser?.name.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it, UserRegisterDTOState.name) },
            label = { Text("name") }
        )

        OutlinedTextField(
            value = createUser?.surname.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it, UserRegisterDTOState.surname) },
            label = { Text("surname") }
        )

        OutlinedTextField(
            value = createUser?.phoneNumber.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it, UserRegisterDTOState.phoneNumber) },
            label = { Text("phone number") }
        )


        OutlinedTextField(
            value = createUser?.secondaryPhoneNumber.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it, UserRegisterDTOState.secondaryPhoneNumber)},
            label = { Text("secondary phone number") }
        )

        OutlinedTextField(
            value = createUser?.email.orEmpty(),
            onValueChange = { userViewModel.onCreateUserChange(it, UserRegisterDTOState.email) },
            label = { Text("email") }
        )

        // === User Type Dropdown ===
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedUserType?.displayname ?: "select user type",
                onValueChange = {},
                label = { Text("user type") },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                userTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayname) },
                        onClick = {
                            expanded = false
                            userViewModel.onCreateUserChange(
                                type.id,
                                UserRegisterDTOState.userTypeId
                            )
                        }
                    )
                }
            }
        }

        // Password
        PasswordField(
            password = createUser?.password.orEmpty(),
            onPasswordChange = {
                userViewModel.onCreateUserChange(it, UserRegisterDTOState.password)
            },
            onDone = { userViewModel.createUser() }
        )

        Button(
            onClick = { userViewModel.createUser() },
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
                "✅ created successfully!",
                color = MaterialTheme.colorScheme.primary
            )
            else -> {}
        }
    }
}