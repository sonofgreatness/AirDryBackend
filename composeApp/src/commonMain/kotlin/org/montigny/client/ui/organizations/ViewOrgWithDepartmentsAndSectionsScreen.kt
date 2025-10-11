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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

@Composable
fun  ViewOrgWithDepartmentsAndSectionsScreen(
    userId: String, navigator: Navigator,
    organizationViewModel: OrganizationDepartmentsAndSectionsViewModel
)
{

    val loadedOrganization by organizationViewModel.loadOrganization.collectAsState()
    val organization = loadedOrganization
    // UI State for dialogs
    var showEditDepartmentDialog by remember { mutableStateOf<DepartmentResponse?>(null) }
    var showEditSectionDialog by remember { mutableStateOf<CreateSectDTO?>(null) }
    var showCreateDepartmentDialog by remember { mutableStateOf(false) }
    var showCreateSectionDialog by remember { mutableStateOf<DepartmentResponse?>(null) }

    LaunchedEffect(userId) {
        organizationViewModel.initializeLoadOrganization(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // ---- ORG HEADER ----
        OrgHeaderBanner(
            orgName = organization?.name ?: "No name loaded" ,
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
                        onEditSection = { showEditSectionDialog = it }
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
            onConfirm = { newName  ,departmentId  ->

                organizationViewModel.editDepartment(
                    newName = newName, departmentId = departmentId
                )
                showEditDepartmentDialog = null }
        )
    }
    // Edit Section
    showEditSectionDialog?.let { sect ->
        EditSectionDialog(
            section = sect,
            onDismiss = { showEditSectionDialog = null },
            onConfirm = { newName, sectionId, departmentId ->
                organizationViewModel.editSection(newName, sectionId, departmentId)
                 showEditSectionDialog = null }
        )
    }
    // Create Department
    if (showCreateDepartmentDialog) {
        CreateDepartmentDialog(
            onDismiss = { showCreateDepartmentDialog = false },
            onConfirm = {name ->
                organizationViewModel.createDepartment(name)
                organizationViewModel.initializeLoadOrganization(userId)
                showCreateDepartmentDialog = false }
        )
    }
    // Create Section
    showCreateSectionDialog?.let { dept ->
        CreateSectionDialog(
            department = dept,
            onDismiss = { showCreateSectionDialog = null },
            onConfirm = {name,departmentId  ->
                organizationViewModel.createSection(name, departmentId)
                /* handle create */ showCreateSectionDialog = null }
        )
    }
}
