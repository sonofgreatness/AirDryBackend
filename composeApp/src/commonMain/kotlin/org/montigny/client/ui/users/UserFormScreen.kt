package org.montigny.client.ui.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        userViewModel.initializeEmptyCreateUse()
        userViewModel.resetCreateUserState()
        userViewModel.populateDepartmentsAndSections()
    }

    // Validation state
    val hasValidationErrors = createUser?.let {
        it.name.isNullOrBlank() ||
                it.surname.isNullOrBlank() ||
                it.email.isNullOrBlank() ||
                it.phoneNumber.isNullOrBlank() || it.phoneNumber!!.length != 8 ||
                (!it.secondaryPhoneNumber.isNullOrBlank() && it.secondaryPhoneNumber!!.length != 8)
                 it.password.length < 5
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
                        NameFields(createUser, focusManager=focusManager , {newValues, state ->
                            userViewModel.onCreateUserChange(newValues, state)})
                        ContactFields(createUser,focusManager=focusManager, {newValues, state ->
                            userViewModel.onCreateUserChange(newValues, state)})
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DropdownFields(
                            selectedUserType, selectedDepartment, selectedSection,
                            expandedUserType, { expandedUserType = it },
                            expandedDepartment, { expandedDepartment = it },
                            expandedSection, { expandedSection = it },
                            userTypes, departments, sections, {newValues, state ->
                                userViewModel.onCreateUserChange(newValues, state)}
                        )

                        ToggleFields(createUser,{newValues, state ->
                            userViewModel.onCreateUserChange(newValues, state)})

                        PasswordField(
                            password = createUser?.password.orEmpty(),
                            focusManager=focusManager,
                                    onPasswordChange = {
                                userViewModel.onCreateUserChange(it, UserRegisterDTOState.password)
                            },
                            onDone = { userViewModel.createUser() }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NameFields(createUser, focusManager=focusManager , {newValues, state ->
                        userViewModel.onCreateUserChange(newValues, state)})
                    ContactFields(createUser,focusManager=focusManager, {newValues, state ->
                        userViewModel.onCreateUserChange(newValues, state)})
                    DropdownFields(
                        selectedUserType, selectedDepartment, selectedSection,
                        expandedUserType, { expandedUserType = it },
                        expandedDepartment, { expandedDepartment = it },
                        expandedSection, { expandedSection = it },
                        userTypes, departments, sections, {newValues, state ->
                            userViewModel.onCreateUserChange(newValues, state)}
                    )

                    ToggleFields(createUser,
                        {newValues, state ->
                            userViewModel.onCreateUserChange(newValues, state)})
                    PasswordField(
                        password = createUser?.password.orEmpty(),
                        focusManager=focusManager,
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
                    enabled = !hasValidationErrors && uiState !is UiState.Loading && uiState !is UiState.Success
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
fun NameFields(createUser: UserRegisterRequest?,
               focusManager: FocusManager,
               onClick :(newValues:String,state: UserRegisterDTOState)
               ->Unit = {_, _->}) {
    val nameError = createUser?.name.isNullOrBlank()
    val surnameError = createUser?.surname.isNullOrBlank()

    OutlinedTextField(
        value = createUser?.name.orEmpty(),
        onValueChange = { onClick(it, UserRegisterDTOState.name) },
        label = { Text("Name", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = nameError,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next)})
    )
    if (nameError) {
        Text("Name cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    OutlinedTextField(
        value = createUser?.surname.orEmpty(),
        onValueChange = { onClick(it, UserRegisterDTOState.surname) },
        singleLine = true,
        label = { Text("Surname", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth(),
        isError = surnameError,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) }
        )
    )
    if (surnameError) {
        Text("Surname cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ContactFields(createUser: UserRegisterRequest?,
                  focusManager: FocusManager,
                  onClick :(newValues:String,state: UserRegisterDTOState)
                  ->Unit = {_, _->}) {
    val phoneError = when {
        createUser?.phoneNumber.isNullOrBlank() -> "Phone number is required"
        createUser.phoneNumber!!.length != 8 -> "Phone number must be 8 digits"
        else -> null
    }
    val secondaryPhoneError = if (!createUser?.secondaryPhoneNumber.isNullOrBlank() &&
        createUser.secondaryPhoneNumber!!.length != 8
    ) "Secondary phone must be 8 digits" else null
    val emailError = createUser?.email.isNullOrBlank()

    OutlinedTextField(
        value = createUser?.phoneNumber.orEmpty(),
        singleLine = true,
        onValueChange = { onClick(it, UserRegisterDTOState.phoneNumber) },
        label = { Text("Phone", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth(),
        isError = phoneError != null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) }
        )
    )
    if (phoneError != null) {
        Text(phoneError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    OutlinedTextField(
        value = createUser?.secondaryPhoneNumber.orEmpty(),
        singleLine = true,
        onValueChange = { onClick(it, UserRegisterDTOState.secondaryPhoneNumber) },
        label = { Text("Secondary Phone", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth(),
        isError = secondaryPhoneError != null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) }
        )
    )
    if (secondaryPhoneError != null) {
        Text(secondaryPhoneError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    OutlinedTextField(
        value = createUser?.email.orEmpty(),
        singleLine = true,
        onValueChange = { onClick(it, UserRegisterDTOState.email) },
        label = { Text("Email", style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.fillMaxWidth(),
        isError = emailError,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) }
        )
    )
    if (emailError) {
        Text("Email cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownFields(
    selectedUserType: User_type?, selectedDepartment: CreateOrgDTO?, selectedSection: CreateSectDTO?,
    expandedUserType: Boolean, onExpandUserType: (Boolean) -> Unit,
    expandedDepartment: Boolean, onExpandDepartment: (Boolean) -> Unit,
    expandedSection: Boolean, onExpandSection: (Boolean) -> Unit,
    userTypes: List<User_type>, departments: List<CreateOrgDTO>, sections: List<CreateSectDTO>,
    onClick :(newValues:String,state: UserRegisterDTOState)
    ->Unit = {_, _->}
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
                            onClick(type.id, UserRegisterDTOState.userTypeId)
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
                            onClick(dept.id.toString(), UserRegisterDTOState.departmentId)
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
                            onClick(section.id.toString(), UserRegisterDTOState.sectionId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleFields(createUser: UserRegisterRequest?,
                 onClick :(newValues:String,state: UserRegisterDTOState)
                 ->Unit = {_, _->}) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Active", style = MaterialTheme.typography.labelMedium)
            Switch(
                checked = createUser?.isActive ?: false,
                onCheckedChange = { onClick(it.toString(),UserRegisterDTOState.isActive(it)) }
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
                onCheckedChange = { onClick(it.toString(),UserRegisterDTOState.isApproved(it)) }
            )
        }
    }
}
