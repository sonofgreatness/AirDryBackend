package org.montigny.client.ui.organizations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.DepartmentResponse
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.organizations.departmentsAndSections.CreateDepartmentDialog
import org.montigny.client.ui.organizations.departmentsAndSections.CreateSectionDialog
import org.montigny.client.ui.organizations.departmentsAndSections.DepartmentItem
import org.montigny.client.ui.organizations.departmentsAndSections.EditDepartmentDialog
import org.montigny.client.ui.organizations.departmentsAndSections.EditSectionDialog
import org.montigny.client.ui.organizations.viewModels.OrganizationDepartmentsAndSectionsViewModel
import org.montigny.client.ui.users.UserRow
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import java.util.UUID
import kotlin.text.Typography.section

@Composable
fun  ViewOrgWithDepartmentsAndSectionsScreen(
    userId: String, navigator: Navigator,
    organizationViewModel: OrganizationDepartmentsAndSectionsViewModel
) {
    val loadedOrganization by organizationViewModel.loadOrganization.collectAsState()
    val organization = loadedOrganization
    val isNetworkAvailable by  organizationViewModel.isNetworkAvailable.collectAsState()
    val isOnlineLoggedIn by  organizationViewModel.isOnlineLoggedIn.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // UI State for dialogs
    var showEditDepartmentDialog by remember { mutableStateOf<DepartmentResponse?>(null) }
    var showEditSectionDialog by remember { mutableStateOf<CreateSectDTO?>(null) }
    var showCreateDepartmentDialog by remember { mutableStateOf(false) }
    var showCreateSectionDialog by remember { mutableStateOf<DepartmentResponse?>(null) }


    var departmentToDelete by remember { mutableStateOf<UUID?>(null) }
    var sectionToDelete by remember { mutableStateOf<UUID?>(null) }

    LaunchedEffect(userId) {
        organizationViewModel.initializeLoadOrganization(userId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // ---- ORG HEADER ----
            OrgHeaderBanner(
                orgName = organization?.name ?: "No name loaded",
                orgLogo = organization?.logo
            )
            Spacer(Modifier.height(16.dp))
            // ---- DEPARTMENTS SECTION ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Departments",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { showCreateDepartmentDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Department")
                }
            }

            if (organization?.departments.isNullOrEmpty()) {
                Text("No Departments Found", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(organization.departments, key = { it.id.toString() }) { dept ->
                        DepartmentItem(
                            department = dept,
                            onEditDepartment = { showEditDepartmentDialog = it },
                            onAddSection = { showCreateSectionDialog = it },
                            onEditSection = { showEditSectionDialog = it },
                            onDeleteSection = { section ->
                                if(!(isNetworkAvailable && isOnlineLoggedIn)) {
                                    navigator.navigateTo(
                                        Destination.Login,
                                        Destination.ViewOrgWithDepartmentsAndSections(userId)
                                    )
                                }else{
                                sectionToDelete = section.id}
                            },
                            onDeleteDepartment = { department ->
                                if(!(isNetworkAvailable && isOnlineLoggedIn)) {
                                    navigator.navigateTo(
                                        Destination.Login,
                                        Destination.ViewOrgWithDepartmentsAndSections(userId)
                                    )
                                }else
                                {departmentToDelete = department.id}
                            }
                        )
                    }
                }
            }
        }
        // ---- DIALOGS ----
        // Edit Department
        showEditDepartmentDialog?.let { dept ->
            EditDepartmentDialog(
                department = dept,
                onDismiss = { showEditDepartmentDialog = null },
                onConfirm = { newName, departmentId ->

                    organizationViewModel.editDepartment(
                        newName = newName, departmentId = departmentId
                    )
                    showEditDepartmentDialog = null
                }
            )
        }
        // Edit Section
        showEditSectionDialog?.let { sect ->
            EditSectionDialog(
                section = sect,
                onDismiss = { showEditSectionDialog = null },
                onConfirm = { newName, sectionId, departmentId ->
                    organizationViewModel.editSection(newName, sectionId, departmentId)
                    showEditSectionDialog = null
                }
            )
        }
        // Create Department
        if (showCreateDepartmentDialog) {
            CreateDepartmentDialog(
                onDismiss = { showCreateDepartmentDialog = false },
                onConfirm = { name ->
                    organizationViewModel.createDepartment(name)
                    organizationViewModel.initializeLoadOrganization(userId)
                    showCreateDepartmentDialog = false
                }
            )
        }
        // Create Section
        showCreateSectionDialog?.let { dept ->
            CreateSectionDialog(
                department = dept,
                onDismiss = { showCreateSectionDialog = null },
                onConfirm = { name, departmentId ->
                    organizationViewModel.createSection(name, departmentId)
                    /* handle create */ showCreateSectionDialog = null
                }
            )
        }
    }

    // Deletion confirmation dialog
    if (departmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { departmentToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            organizationViewModel.deleteDepartment(departmentToDelete!!)
                            departmentToDelete = null
                        }
                    }
                ) {
                    androidx.compose.material3.Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { departmentToDelete = null }) {
                    androidx.compose.material3.Text("Cancel")
                }
            },
            title = { androidx.compose.material3.Text("Delete Department") },
            text = { androidx.compose.material3.Text("Are you sure you want to delete this department? This action cannot be undone.") },
            icon = {
                androidx.compose.material.Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
// Show backend error messages gracefully
    val errorMessageForDepartment by organizationViewModel.errorMessageForDepartment.collectAsState()
    if (!errorMessageForDepartment.isNullOrEmpty()) {
        LaunchedEffect(errorMessageForDepartment) {
            snackbarHostState.showSnackbar(
                message = errorMessageForDepartment ?: "An unexpected error occurred.",
                duration = SnackbarDuration.Long
            )
            organizationViewModel.clearDepartmentErrorMessage()
        }
    }
    // Deletion confirmation dialog
    if (sectionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sectionToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            organizationViewModel.deleteSection(sectionToDelete!!)
                            sectionToDelete = null
                        }
                    }
                ) {
                    androidx.compose.material3.Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { sectionToDelete = null }) {
                    androidx.compose.material3.Text("Cancel")
                }
            },
            title = { androidx.compose.material3.Text("Delete Section") },
            text = { androidx.compose.material3.Text("Are you sure you want to delete this Section? This action cannot be undone.") },
            icon = {
                androidx.compose.material.Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }

// Show backend error messages gracefully
    val errorMessageForSection  by organizationViewModel.errorMessageForSection.collectAsState()
    if (!errorMessageForSection.isNullOrEmpty()) {
        LaunchedEffect(errorMessageForSection) {
            snackbarHostState.showSnackbar(
                message = errorMessageForSection ?: "An unexpected error occurred.",
                duration = SnackbarDuration.Long
            )
            organizationViewModel.clearSectionErrorMessage()
        }
    }
}