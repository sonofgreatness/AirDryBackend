package org.montigny.client.ui.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.util.stateClasses.UiState
import org.montigny.client.util.stateClasses.UserRegisterDTOState

@Composable
fun  EditUserFormScreen(
    userId : String,
    userViewModel: UserViewModel,
    navigator: Navigator
) {

    val updateUser by userViewModel.updateUser.collectAsState()
    val uiState by userViewModel.updateUserState.collectAsState()
    val userTypes by userViewModel.useTypes.collectAsState()
    val departments by userViewModel.departments.collectAsState()
    val sections by userViewModel.sections.collectAsState()

    // Dropdown state
    var expandedUserType by remember { mutableStateOf(false) }
    var expandedDepartment by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userViewModel.initializeUpdateUser(userId)
    }
    val selectedUserType by remember(updateUser, userTypes) {
        derivedStateOf { userTypes.find { it.id == updateUser?.userTypeId?.toString() } }
    }
    val selectedDepartment by remember(updateUser, departments) {
        derivedStateOf { departments.find { it.id == updateUser?.departmentId } }
    }
    val selectedSection by remember(updateUser, sections) {
        derivedStateOf { sections.find { it.id == updateUser?.sectionId } }
    }


    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val hasValidationErrors = updateUser?.let {
        it.name.isNullOrBlank() ||
                it.surname.isNullOrBlank() ||
                it.email.isNullOrBlank() ||
                it.phoneNumber.isNullOrBlank() || it.phoneNumber!!.length != 8 ||
                (!it.nationalId.isNullOrBlank() && it.nationalId!!.length != 13)
                ||it.password.length < 5
    } ?: true

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val isWideScreen = maxWidth > 800.dp

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
        ) {
            // --- Page Header ---
            Text(
                "Edit User",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Fill in details to edit the new user account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // --- Form Layout ---
            if (isWideScreen) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        NameFields(updateUser, focusManager=focusManager) { newValues, state ->
                            userViewModel.onUpdateUserChange(newValues, state)
                        }
                        ContactFields(updateUser,focusManager=focusManager) { newValues, state ->
                            userViewModel.onUpdateUserChange(newValues, state)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DropdownFields(
                            selectedUserType, selectedDepartment, selectedSection,
                            expandedUserType, { expandedUserType = it },
                            expandedDepartment, { expandedDepartment = it },
                            expandedSection, { expandedSection = it },
                            userTypes, departments, sections,{newValues, state ->
                                userViewModel.onUpdateUserChange(newValues, state)}
                        )
                        ToggleFields(updateUser,{newValues, state ->
                            userViewModel.onUpdateUserChange(newValues, state)})

                        PasswordField(
                            password = updateUser?.password.orEmpty(),
                            focusManager=focusManager,
                            onPasswordChange = {
                                userViewModel.onUpdateUserChange(it, UserRegisterDTOState.password)
                            },
                            onDone = { userViewModel.updateUser() }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NameFields(updateUser,focusManager=focusManager,{newValues, state ->
                        userViewModel.onUpdateUserChange(newValues, state)})
                    ContactFields(updateUser,focusManager=focusManager, {newValues, state ->
                        userViewModel.onUpdateUserChange(newValues, state)})
                    DropdownFields(
                        selectedUserType, selectedDepartment, selectedSection,
                        expandedUserType, { expandedUserType = it },
                        expandedDepartment, { expandedDepartment = it },
                        expandedSection, { expandedSection = it },
                        userTypes, departments, sections,{newValues, state ->
                            userViewModel.onUpdateUserChange(newValues, state)})

                    ToggleFields(updateUser) { newValues, state ->
                        userViewModel.onUpdateUserChange(newValues, state)
                    }
                    PasswordField(
                        password = updateUser?.password.orEmpty(),
                        focusManager=focusManager,
                        onPasswordChange = {
                            userViewModel.onUpdateUserChange(it, UserRegisterDTOState.password)
                        },
                        onDone = { userViewModel.updateUser()}
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            // --- Actions ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { userViewModel.updateUser() },
                    enabled = !hasValidationErrors && uiState !is UiState.Loading && uiState !is UiState.Success
                ) {
                    Text(if (uiState is UiState.Loading) "Editing..." else "Edit")
                }
                OutlinedButton(onClick = { navigator.navigateTo(Destination.Users) }) {
                    Text("Back")
                }
            }
            Spacer(Modifier.height(16.dp))
            // --- Status Messages ---
            when (uiState) {
                is UiState.Error -> Text(
                    (uiState as UiState.Error).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                is UiState.Success -> Text(
                    "✅ User editted successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                else -> {}
            }
        }
    }
}