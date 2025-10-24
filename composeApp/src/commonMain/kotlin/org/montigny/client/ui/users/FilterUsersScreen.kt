package org.montigny.client.ui.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.Theme.White
import org.montigny.client.ui.users.viewModels.FilterUsersViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.ui.work.viewModel.WorkDataFiltersDTOState
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterUsersScreen(
    navigator: Navigator,
    viewModel: FilterUsersViewModel,
    userViewModel: UserViewModel,
) {
    val filtersDTO by viewModel.filterDto.collectAsState()
    val currentPage by  viewModel.currentPage.collectAsState()
    val pageSize = DEFAULT_PAGE_SIZE
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val totalPages by viewModel.totalPages.collectAsState()
    val scope = rememberCoroutineScope()
    val users  by viewModel.users.collectAsState()
    val listState = rememberLazyListState()
    val isDeleting_ by userViewModel.isDeleting.collectAsState()
    var userToDelete by remember { mutableStateOf<UUID?>(null) }

    LaunchedEffect(viewModel.snackbarEvent) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(viewModel){
        viewModel.populateUsers(pageSize = pageSize)
        viewModel.populateDepartmentsAndSections()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { androidx.compose.material3.Text("Find Worker") },
                navigationIcon = {
                    IconButton(onClick = {navigator.navigateTo(Destination.Users)}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ){paddingValues ->
        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    FilterSectionTopRowUser(filtersDTO, viewModel, onToggle = { includeInactive ->
                        viewModel.onChangeFilter(
                            WorkDataFiltersDTOState.IncludeInactiveWorkers(includeInactive),
                            ""
                        )
                    })
                    Spacer(modifier = Modifier.height(6.dp))
                    FilterSectionBottomRowUser(filtersDTO, viewModel)
                }
                Spacer(modifier = Modifier.width(12.dp))
                // --- Right Column: Clear Filters Button ---
                Button(
                    onClick = { viewModel.clearAllFilters() },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = White
                    )
                ) {
                    Text(color = White,
                       text = "Clear All Filters")
                }
            }
                Spacer(modifier = Modifier.width(12.dp))
                // ------- LIST REGION -------
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 10.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (users.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.Text(
                                "No users available",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(users) { index, user ->
                                UserRow(
                                    user = user,
                                    onEdit = {
                                        userViewModel.resetUpdateUserState()
                                        navigator.navigateTo(Destination.EditUser(user.id.toString()))
                                    },
                                    {
                                        navigator.navigateTo(
                                            Destination.ViewUserWork(user.id.toString()),
                                            Destination.Users
                                        )
                                    },
                                    onDelete = { userToDelete = user.id },
                                    isDeleting = isDeleting_
                                )
                                val hasMorePages = currentPage < totalPages
                                if (index >= users.lastIndex - 2 && hasMorePages) {
                                    scope.launch { viewModel.loadNextPage(pageSize)
                                    }
                                }
                            }
                            item {
                                if (currentPage < totalPages) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                                } else {
                                    androidx.compose.material3.Text(
                                        "End of Records",
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
            }
        }//column

    }//scaffold
    // Deletion confirmation dialog
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            userViewModel.deleteUserById(userToDelete!!)
                            userToDelete = null
                        }
                    }
                ) {
                    androidx.compose.material3.Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    androidx.compose.material3.Text("Cancel")
                }
            },
            title = { androidx.compose.material3.Text("Delete User") },
            text = { androidx.compose.material3.Text("Are you sure you want to delete this user? This action cannot be undone.") },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
        )
    }

// Show backend error messages gracefully
    val errorMessage by userViewModel.errorMessage.collectAsState()
    if (!errorMessage.isNullOrEmpty()) {
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage ?: "An unexpected error occurred.",
                duration = SnackbarDuration.Long
            )
            userViewModel.clearErrorMessage()
        }
    }




}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun  FilterSectionTopRowUser(
     filtersDTO: WorkDataFiltersDTO,
     viewModel: FilterUsersViewModel,
     onToggle: (Boolean) -> Unit){
    var expandedDepartment by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(false) }

    val departments by viewModel.departments.collectAsState()
    val sections by viewModel.sections.collectAsState()

    val selectedDepartment by remember(filtersDTO, departments) {
        derivedStateOf { departments.find { it.id == filtersDTO.filterDepartmentId } }
    }
    val selectedSection by remember(filtersDTO, sections) {
        derivedStateOf { sections.find { it.id == filtersDTO.filterSectionId } }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if(filtersDTO.includeInactiveWorkers)
                "Status : Inactive"
                  else
                    "Status : Active"
                , style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = filtersDTO.includeInactiveWorkers,
                onCheckedChange = { onToggle(it) },
            )
        }
        // === Department Dropdown ===
        ExposedDropdownMenuBox(
            expanded = expandedDepartment,
            onExpandedChange = { expandedDepartment = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedDepartment?.displayName ?: "Select department",
                onValueChange = {},
                label = { androidx.compose.material3.Text("Department") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDepartment) }
            )
            ExposedDropdownMenu(
                expanded = expandedDepartment,
                onDismissRequest = { expandedDepartment = false }
            ) {
                departments.forEach { dept ->
                    DropdownMenuItem(
                        text = { androidx.compose.material3.Text(dept.displayName) },
                        onClick = {
                            expandedDepartment = false
                            viewModel.onChangeFilter(
                                WorkDataFiltersDTOState.FilterDepartment,
                                dept.id.toString()
                            )
                        }
                    )
                }
            }
        }

        // === Section Dropdown ===
        ExposedDropdownMenuBox(
            expanded = expandedSection,
            onExpandedChange = { expandedSection = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedSection?.displayName ?: "Select section",
                onValueChange = {},
                label = { androidx.compose.material3.Text("Section") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSection) }
            )
            ExposedDropdownMenu(
                expanded = expandedSection,
                onDismissRequest = { expandedSection = false }
            ) {
                sections.forEach { section ->
                    DropdownMenuItem(
                        text = { androidx.compose.material3.Text(section.displayName) },
                        onClick = {
                            expandedSection = false
                            viewModel.onChangeFilter(
                                WorkDataFiltersDTOState.FilterSection,
                                section.id.toString()
                            )
                        }
                    )
                }
            }
        }
        // === Worker name ===
        OutlinedTextField(
            value = filtersDTO.filterName ?: "",
            onValueChange = {
                viewModel.onChangeFilter(WorkDataFiltersDTOState.FilterName, it)
            },
            label = { androidx.compose.material3.Text("Worker name") },
            modifier = Modifier.weight(1f)
        )
    }
 }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  FilterSectionBottomRowUser(
    filtersDTO: WorkDataFiltersDTO,
    viewModel: FilterUsersViewModel){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ){
    OutlinedTextField(
        value = filtersDTO.filterSurname ?: "",
        onValueChange = {
            viewModel.onChangeFilter(WorkDataFiltersDTOState.FilterSurname, it)
        },
        label = { androidx.compose.material3.Text("Worker Surname") },
        modifier = Modifier.weight(1f)
    )
    OutlinedTextField(
        value = filtersDTO.filterPhoneNumber ?: "",
        onValueChange = {
            viewModel.onChangeFilter(WorkDataFiltersDTOState.FilterPhone, it)
        },
        label = { androidx.compose.material3.Text("Phone number") },
        modifier = Modifier.weight(1f)
    )
    OutlinedTextField(
        value = filtersDTO.filterSecondaryPhoneNumber?: "",
        onValueChange = {
            viewModel.onChangeFilter(WorkDataFiltersDTOState.FilterSecondaryPhone, it)
        },
        label = { Text("Secondary phone number.") },
        modifier = Modifier.weight(1f)
    )}
}