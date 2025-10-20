package org.montigny.client.ui.work

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.Work.*
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

            FilterSection(filtersDTO, workViewModel)

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

/** ✅ FILTER SECTION WITH MATERIAL DATE PICKER **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    filtersDTO: WorkDataFiltersDTO,
    workViewModel: WorkViewModel
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val openDialog = remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openDialog.value = false }) {
                        Text("Cancel")
                    }
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

        OutlinedTextField(
            value = filtersDTO.filterDepartment ?: "",
            onValueChange = {
                workViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterDepartment, it)
            },
            label = { Text("Department") },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = filtersDTO.filterSection ?: "",
            onValueChange = {
                workViewModel.onChangeFilter(WorkDataFiltersDTOState.FilterSection, it)
            },
            label = { Text("Section") },
            modifier = Modifier.weight(1f)
        )

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
        Text(String.format("%.2f m³", stack.volume), style = MaterialTheme.typography.bodyMedium)
    }
}

/** ✅ USER ROW **/
@Composable
private fun UserRow(user: UserInfoDTO) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${user.name} ${user.surname}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(user.phoneNumber, style = MaterialTheme.typography.labelSmall)
        }
        Text(String.format("%.2f m³", user.personalCubes), style = MaterialTheme.typography.bodyMedium)
    }
}
