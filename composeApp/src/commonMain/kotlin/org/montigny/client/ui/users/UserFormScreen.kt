package org.montigny.client.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.com.client.database.User_type
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.util.stateClasses.UiState
import org.montigny.client.util.stateClasses.UserRegisterDTOState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(userViewModel: UserViewModel, navigator: Navigator) {
    val createUser by userViewModel.createUser.collectAsState()
    val uiState by userViewModel.createUserState.collectAsState()
    val userTypes by userViewModel.useTypes.collectAsState()
    val departments by userViewModel.departments.collectAsState()
    val sections by userViewModel.sections.collectAsState()

    // Dropdown state
    var expandedUserType by remember { mutableStateOf(false) }
    var expandedDepartment by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(false) }

    // Currently selected values
    val selectedUserType = userTypes.find { it.id == createUser?.userTypeId?.toString() }
    val selectedDepartment = departments.find { it.id == createUser?.departmentId }
    val selectedSection = sections.find { it.id == createUser?.sectionId }

    LaunchedEffect(Unit) {
        userViewModel.initializeEmptyCreateUse()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val isWideScreen = maxWidth > 800.dp

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            // --- Page Header ---
            Text(
                "Create User",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Fill in details to register a new user account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // --- Form Layout ---
            if (isWideScreen) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        NameFields(createUser, userViewModel)
                        ContactFields(createUser, userViewModel)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DropdownFields(
                            selectedUserType, selectedDepartment, selectedSection,
                            expandedUserType, { expandedUserType = it },
                            expandedDepartment, { expandedDepartment = it },
                            expandedSection, { expandedSection = it },
                            userTypes, departments, sections, userViewModel
                        )

                        ToggleFields(createUser, userViewModel)

                        PasswordField(
                            password = createUser?.password.orEmpty(),
                            onPasswordChange = {
                                userViewModel.onCreateUserChange(it, UserRegisterDTOState.password)
                            },
                            onDone = { userViewModel.createUser() }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NameFields(createUser, userViewModel)
                    ContactFields(createUser, userViewModel)
                    DropdownFields(
                        selectedUserType, selectedDepartment, selectedSection,
                        expandedUserType, { expandedUserType = it },
                        expandedDepartment, { expandedDepartment = it },
                        expandedSection, { expandedSection = it },
                        userTypes, departments, sections, userViewModel
                    )
                    
                    ToggleFields(createUser, userViewModel)
                    PasswordField(
                        password = createUser?.password.orEmpty(),
                        onPasswordChange = {
                            userViewModel.onCreateUserChange(it, UserRegisterDTOState.password)
                        },
                        onDone = { userViewModel.createUser() }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- Actions ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { userViewModel.createUser() },
                    enabled = uiState !is UiState.Loading
                ) {
                    Text(if (uiState is UiState.Loading) "Creating..." else "Create")
                }
                OutlinedButton(onClick = { navigator.navigateTo(Destination.Users) }) {
                    Text("Cancel")
                }
                if (uiState is UiState.Success) {
                    OutlinedButton(onClick = { userViewModel.initializeEmptyCreateUse() }) {
                        Text("Create Another")
                    }
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
                    "✅ User created successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                else -> {}
            }
        }
    }
}

// --- Helpers for cleaner layout ---
@Composable
private fun NameFields(createUser: UserRegisterRequest?, vm: UserViewModel) {
    OutlinedTextField(
        value = createUser?.name.orEmpty(),
        onValueChange = { vm.onCreateUserChange(it, UserRegisterDTOState.name) },
        label = { Text("Name", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = createUser?.surname.orEmpty(),
        onValueChange = { vm.onCreateUserChange(it, UserRegisterDTOState.surname) },
        label = { Text("Surname", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ContactFields(createUser: UserRegisterRequest?, vm: UserViewModel) {
    OutlinedTextField(
        value = createUser?.phoneNumber.orEmpty(),
        onValueChange = { vm.onCreateUserChange(it, UserRegisterDTOState.phoneNumber) },
        label = { Text("Phone", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = createUser?.secondaryPhoneNumber.orEmpty(),
        onValueChange = { vm.onCreateUserChange(it, UserRegisterDTOState.secondaryPhoneNumber) },
        label = { Text("Secondary Phone", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = createUser?.email.orEmpty(),
        onValueChange = { vm.onCreateUserChange(it, UserRegisterDTOState.email) },
        label = { Text("Email", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownFields(
    selectedUserType: User_type?, selectedDepartment: CreateOrgDTO?, selectedSection: CreateSectDTO?,
    expandedUserType: Boolean, onExpandUserType: (Boolean) -> Unit,
    expandedDepartment: Boolean, onExpandDepartment: (Boolean) -> Unit,
    expandedSection: Boolean, onExpandSection: (Boolean) -> Unit,
    userTypes: List<User_type>, departments: List<CreateOrgDTO>, sections: List<CreateSectDTO>,
    vm: UserViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // === User Type Dropdown ===
        ExposedDropdownMenuBox(
            expanded = expandedUserType,
            onExpandedChange = { onExpandUserType(!expandedUserType) }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedUserType?.displayname ?: "Select user type",
                onValueChange = {},
                label = { Text("User type", style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedUserType,
                onDismissRequest = { onExpandUserType(false) }
            ) {
                userTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayname, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onExpandUserType(false)
                            vm.onCreateUserChange(type.id, UserRegisterDTOState.userTypeId)
                        }
                    )
                }
            }
        }

        // === Department Dropdown ===
        ExposedDropdownMenuBox(
            expanded = expandedDepartment,
            onExpandedChange = { onExpandDepartment(!expandedDepartment) }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedDepartment?.displayName ?: "Select department",
                onValueChange = {},
                label = { Text("Department", style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDepartment,
                onDismissRequest = { onExpandDepartment(false) }
            ) {
                departments.forEach { dept ->
                    DropdownMenuItem(
                        text = { Text(dept.displayName, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onExpandDepartment(false)
                            vm.onCreateUserChange(dept.id.toString(), UserRegisterDTOState.departmentId)
                        }
                    )
                }
            }
        }

        // === Section Dropdown ===
        ExposedDropdownMenuBox(
            expanded = expandedSection,
            onExpandedChange = { onExpandSection(!expandedSection) }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedSection?.displayName ?: "Select section",
                onValueChange = {},
                label = { Text("Section", style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedSection,
                onDismissRequest = { onExpandSection(false) }
            ) {
                sections.forEach { section ->
                    DropdownMenuItem(
                        text = { Text(section.displayName, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onExpandSection(false)
                            vm.onCreateUserChange(section.id.toString(), UserRegisterDTOState.sectionId)
                        }
                    )
                }
            }
        }
    }

}
@Composable
private fun ToggleFields(createUser: UserRegisterRequest?, vm: UserViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Active", style = MaterialTheme.typography.labelMedium)
            Switch(
                checked = createUser?.isActive ?: false,
                onCheckedChange = { vm.onCreateUserChange(it.toString(),UserRegisterDTOState.isActive(it)) }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Approved", style = MaterialTheme.typography.labelMedium)
            Switch(
                checked = createUser?.isApproved ?: false,
                onCheckedChange = { vm.onCreateUserChange(it.toString(),UserRegisterDTOState.isApproved(it)) }
            )
        }
    }
}
