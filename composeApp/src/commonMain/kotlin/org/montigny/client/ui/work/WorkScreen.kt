package org.montigny.client.ui.work

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.montigny.client.dto.Work.StackInfoDTO
import org.montigny.client.dto.Work.UserInfoDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.Work.WorkSummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.work.viewModel.WorkDataFiltersDTOState
import org.montigny.client.ui.work.viewModel.LoadWorkViewModel
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScreen(
    loadWorkViewModel: LoadWorkViewModel, navigator: Navigator
) {
    val scope = rememberCoroutineScope()
    val workSummaries by loadWorkViewModel.dailyWorkSummaries.collectAsState()
    val filtersDTO by loadWorkViewModel.filterDto.collectAsState()

    var currentPage by remember { mutableStateOf(1) }
    val pageSize = DEFAULT_PAGE_SIZE

    val expandedDays = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        loadWorkViewModel.populateDailyWorkSummaries(page = currentPage, pageSize = pageSize)
        loadWorkViewModel.populateDepartmentsAndSections()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            HeaderSection(filtersDTO, onToggle = { includeInactive ->
                loadWorkViewModel.onChangeFilter(
                    WorkDataFiltersDTOState.IncludeInactiveWorkers(includeInactive),
                    ""
                )
            })
            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- Left Column: the two rows ---
                Column(modifier = Modifier.weight(1f)) {
                    FilterSectionTopRow(filtersDTO, loadWorkViewModel)
                    Spacer(modifier = Modifier.height(6.dp))
                    FilterSectionBottomRow(filtersDTO, loadWorkViewModel)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // --- Right Column: Clear Filters Button ---
                Button(
                    onClick = { loadWorkViewModel.clearAllFilters() },
                    modifier = Modifier.height(40.dp) // fixed height
                ) {
                    Text("Clear All Filters")
                }
            }
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
                            },
                            navigator
                        )

                        if (index >= workSummaries.lastIndex - 2) {
                            scope.launch {
                                currentPage++
                                loadWorkViewModel.populateDailyWorkSummaries(
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
    loadWorkViewModel: LoadWorkViewModel
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val openDialog = remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    var expandedDepartment by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(false) }

    val departments by loadWorkViewModel.departments.collectAsState()
    val sections by loadWorkViewModel.sections.collectAsState()

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
                                loadWorkViewModel.onChangeFilter(
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
                            loadWorkViewModel.onChangeFilter(
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
                            loadWorkViewModel.onChangeFilter(
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
                loadWorkViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterName, it)
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
    loadWorkViewModel: LoadWorkViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = filtersDTO.filterSurname ?: "",
            onValueChange = {
                loadWorkViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterSurname, it)
            },
            label = { Text("Worker Surname") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = filtersDTO.filterPhoneNumber ?: "",
            onValueChange = {
                loadWorkViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterPhone, it)
            },
            label = { Text("Phone number") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = filtersDTO.filterSecondaryPhoneNumber?: "",
            onValueChange = {
                loadWorkViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterSecondaryPhone, it)
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
    onToggleExpand: (Boolean) -> Unit,
    navigator: Navigator
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
                        WorkSummaryItem(work, navigator = navigator)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}
/** ✅ INLINE STAT DISPLAY **/
@Composable
fun StatInline(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(end = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}

/** ✅ WORK SUMMARY ITEM — Enhanced UI **/
@Composable
private fun WorkSummaryItem(work: WorkSummaryDTO, navigator: Navigator) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // -------------------------
            // Header: Date & Stats
            // -------------------------
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📅 ${work.workDate}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatInline("📦 Volume", String.format("%.2f m³", work.totalVolume))
                        StatInline("🗂 Stacks", work.totalStacks.toString())
                        StatInline("👷 Users", work.totalUsers.toString())
                        work.capturedBy?.let { StatInline("✍️ Captured by", it) }
                        work.capturedAt?.let { StatInline("⏱ Captured at", it) }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        work.updatedBy?.let { StatInline("🔄 Updated by", it) }
                        if (work.capturedAt != work.updatedAt) {
                            work.updatedAt?.let { StatInline("⏱ Updated at", it) }
                        }
                    }
                    IconButton(
                        onClick = {
                            navigator.navigateTo(
                                Destination.EditWork(work.id.toString())
                            )
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Work Entry",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------
            // Stacks Section
            // -------------------------
            SectionHeader(title = "Stacks Worked", subtitle = "Cubes")
            Column(modifier = Modifier.fillMaxWidth().padding(start = 6.dp)) {
                work.stacks.forEach { StackRow(it) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------
            // Users Section
            // -------------------------
            SectionHeader(title = "Workers", subtitle = "Personal Cubes")
            Column(modifier = Modifier.fillMaxWidth().padding(start = 6.dp)) {
                work.users.forEach { UserRow(it, navigator) }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        Text(subtitle, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
    }
}

/** ✅ STACK ROW **/
@Composable
fun StackRow(stack: StackInfoDTO) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stack.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(stack.stackCodeDisplayName, style = MaterialTheme.typography.labelSmall)
        }
        Text(String.format("%.3f m³", stack.volume), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun UserRow(user: UserInfoDTO, navigator: Navigator) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp).clickable{
                navigator.navigateTo(Destination.ViewUserWork(user.id.toString())
                , Destination.Work)
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Left: Avatar Circle with Initials ---
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (user.isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${user.name.firstOrNull() ?: '?'}${user.surname.firstOrNull() ?: '?'}".uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- Middle: User info ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.name} ${user.surname}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.phoneNumber,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Active / Approved badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("Active", user.isActive, Icons.Default.CheckCircle)
                    StatusChip("Approved", user.isApproved, Icons.Default.Verified)
                }
            }

            // --- Right: Volume ---
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.3f m³", user.personalCubes),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "\uD83D\uDCC8 Volume",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}

@Composable
fun StatusChip(label: String, status: Boolean, icon: ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (status) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (status) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (status) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.outline
            )
        )
    }
}

