package org.montigny.client.ui.work

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.Work.StackInfoDTO
import org.montigny.client.dto.Work.UserInfoDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.Work.WorkSummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScreen(
    workViewModel: WorkViewModel
) {
    val scope = rememberCoroutineScope()
    val workSummaries by workViewModel.dailyWorkSummaries.collectAsState()
    val filtersDTO by workViewModel.filterDto.collectAsState()

    var currentPage by remember { mutableStateOf(1) }
    val pageSize = DEFAULT_PAGE_SIZE
    val expandedDays = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        workViewModel.populateDailyWorkSummaries(page = currentPage, pageSize = pageSize)
        workViewModel.populateDepartmentsAndSections()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            HeaderSection(filtersDTO, onToggle = { includeInactive ->
                workViewModel.onChangeFilter(
                    WorkDataFiltersDTOState.IncludeInactiveWorkers(includeInactive),
                    ""
                )
            })
            Spacer(Modifier.height(18.dp))

            FilterSectionTopRow(filtersDTO, workViewModel)
            FilterSectionBottomRow(filtersDTO, workViewModel)
            Spacer(Modifier.height(18.dp))

            if (workSummaries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records yet", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(workSummaries) { index, daySummary ->
                        WorkDaySummaryCard(
                            summary = daySummary,
                            expanded = expandedDays[daySummary.dayLabel] == true,
                            onToggleExpand = { expanded ->
                                expandedDays[daySummary.dayLabel] = expanded
                            }
                        )

                        if (index >= workSummaries.lastIndex - 2) {
                            scope.launch {
                                currentPage++
                                workViewModel.populateDailyWorkSummaries(
                                    page = currentPage,
                                    pageSize = pageSize
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** ✅ HEADER SECTION **/
@Composable
private fun HeaderSection(filtersDTO: WorkDataFiltersDTO, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Daily Work Records",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Include deactivated users", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = filtersDTO.includeInactiveWorkers,
                onCheckedChange = { onToggle(it) },
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSectionTopRow(
    filtersDTO: WorkDataFiltersDTO,
    workViewModel: WorkViewModel
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val openDialog = remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    var expandedDepartment by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(false) }

    val departments by workViewModel.departments.collectAsState()
    val sections by workViewModel.sections.collectAsState()

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
        // === Date Picker ===
        OutlinedTextField(
            value = filtersDTO.filterDate ?: "",
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            modifier = Modifier.weight(1f),
            trailingIcon = {
                IconButton(onClick = { openDialog.value = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pick date"
                    )
                }
            }
        )

        if (openDialog.value) {
            DatePickerDialog(
                onDismissRequest = { openDialog.value = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dateState.selectedDateMillis?.let { millis ->
                                val date = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                val formatted = date.format(dateFormatter)
                                workViewModel.onChangeFilter(
                                    WorkDataFiltersDTOState.FilterDate,
                                    formatted
                                )
                            }
                            openDialog.value = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { openDialog.value = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(
                    state = dateState,
                    showModeToggle = false,
                    modifier = Modifier.padding(8.dp),
                    title = { Text("Select Date") }
                )
            }
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
                label = { Text("Department") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDepartment) }
            )
            ExposedDropdownMenu(
                expanded = expandedDepartment,
                onDismissRequest = { expandedDepartment = false }
            ) {
                departments.forEach { dept ->
                    DropdownMenuItem(
                        text = { Text(dept.displayName) },
                        onClick = {
                            expandedDepartment = false
                            workViewModel.onChangeFilter(
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
                label = { Text("Section") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSection) }
            )
            ExposedDropdownMenu(
                expanded = expandedSection,
                onDismissRequest = { expandedSection = false }
            ) {
                sections.forEach { section ->
                    DropdownMenuItem(
                        text = { Text(section.displayName) },
                        onClick = {
                            expandedSection = false
                            workViewModel.onChangeFilter(
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
                workViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterName, it)
            },
            label = { Text("Worker name") },
            modifier = Modifier.weight(1f)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSectionBottomRow(
    filtersDTO: WorkDataFiltersDTO,
    workViewModel: WorkViewModel
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = filtersDTO.filterSurname ?: "",
            onValueChange = {
                workViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterSurname, it)
            },
            label = { Text("Worker Surname") },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = filtersDTO.filterPhoneNumber ?: "",
            onValueChange = {
                workViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterPhone, it)
            },
            label = { Text("Phone number") },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = filtersDTO.filterSecondaryPhoneNumber?: "",
            onValueChange = {
                workViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterSecondaryPhone, it)
            },
            label = { Text("Secondary phone number.") },
            modifier = Modifier.weight(1f)
        )
    }
}



/** ✅ DAY SUMMARY CARD **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkDaySummaryCard(
    summary: WorkDaySummaryDTO,
    expanded: Boolean,
    onToggleExpand: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(summary.dayLabel, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatInline("Volume (Cubes)", String.format("%.2f m³", summary.totalVolume))
                        StatInline("Stacks", summary.totalStacks.toString())
                        StatInline("Users", summary.totalUsers.toString())
                    }
                }
                TextButton(onClick = { onToggleExpand(!expanded) }) {
                    Text(if (expanded) "Collapse" else "Expand")
                }
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    summary.workList.forEach { work ->
                        WorkSummaryItem(work)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

/** ✅ INLINE STAT DISPLAY **/
@Composable
private fun StatInline(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(end = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}

/** ✅ WORK SUMMARY ITEM **/
@Composable
private fun WorkSummaryItem(work: WorkSummaryDTO) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(work.workDate, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatInline("Volume (Cubes)", String.format("%.2f m³", work.totalVolume))
                    StatInline("Stacks", work.totalStacks.toString())
                    StatInline("Users", work.totalUsers.toString())
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Stacks Worked", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Text("Cubes", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
        Column(modifier = Modifier.fillMaxWidth().padding(start = 6.dp)) {
            work.stacks.forEach { stack -> StackRow(stack) }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Workers", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Text("Personal Cubes", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
        Column(modifier = Modifier.fillMaxWidth().padding(start = 6.dp)) {
            work.users.forEach { user -> UserRow(user) }
        }
    }
}

/** ✅ STACK ROW **/
@Composable
private fun StackRow(stack: StackInfoDTO) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stack.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(stack.stackCodeDisplayName, style = MaterialTheme.typography.labelSmall)
        }
        Text(String.format("%.3f m³", stack.volume), style = MaterialTheme.typography.bodyMedium)
    }
}

/** ✅ USER ROW **/
@Composable
private fun UserRow(user: UserInfoDTO) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${user.name} ${user.surname} isActive :${user.isActive} isApproved ${user.isApproved}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(user.phoneNumber, style = MaterialTheme.typography.labelSmall)
        }
        Text(String.format("%.3f m³", user.personalCubes), style = MaterialTheme.typography.bodyMedium)
    }
}
