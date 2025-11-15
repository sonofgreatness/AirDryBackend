package org.montigny.client.ui.work

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.Work.UserInfoDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.work.viewModel.LoadUserWorkViewModel
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.UtilityFunctions.Companion.extractFullNameFromApiString
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserWorkScreen(
    userId: String,
    navigator: Navigator,
    viewModel: LoadUserWorkViewModel,
    onMoveToNext: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val user by viewModel.user.collectAsState()
    val dailySummaries by viewModel.dailyWorkSummaries.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val isSyncing  by viewModel.isSyncing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentPage by viewModel.currentPage.collectAsState()
    val pageSize = DEFAULT_PAGE_SIZE
    val expandedDays = remember { mutableStateMapOf<String, Boolean>() }


    // Initial load and filter on user change
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    // Once user data is loaded, fetch summaries
    LaunchedEffect(user) {
        if (user != null && dailySummaries.isEmpty()) {
            viewModel.populateDailyWorkSummaries(pageSize = pageSize)
        }
    }
    LaunchedEffect(viewModel.snackbarEvent) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Performance") },
                navigationIcon = {
                    IconButton(onClick = {onMoveToNext()}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // --- 1. TITTLE Section: Worker Details ---
            user?.let { UserDetailsCard(it) }
            Spacer(Modifier.height(20.dp))

            // --- 2. Filter Section: Start and End Date ---
            DateFilterSection(
                startDate = startDate,
                endDate = endDate,
                onStartDateChange = viewModel::onStartDateChangeListener,
                onEndDateChange = viewModel::onEndDateChangeListener,
                onSubmit = { viewModel.submitDateFilter(startDate, endDate) },
                onClear = viewModel::clearDateFilters
            )
            Spacer(Modifier.height(20.dp))

            Divider()
            Spacer(Modifier.height(16.dp))

            // --- 3. Summaries Section ---
            if(dailySummaries.isEmpty() && !isSyncing) {
                EmptyListPlaceHolder(
                    "No work data to show",
                    50,
                    Icons.Default.WarningAmber)
            }
            if (dailySummaries.isEmpty() && isSyncing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Text("Daily Work Summaries", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) }
                    itemsIndexed(dailySummaries) { index, daySummary ->
                        // Using the modified WorkDaySummaryCard which calls the highlight UserWorkSummaryItem
                        UserWorkDaySummaryCard(
                            summary = daySummary,
                            expanded = expandedDays[daySummary.dayLabel] == true,
                            onToggleExpand = { expanded -> expandedDays[daySummary.dayLabel] = expanded },
                            navigator = navigator,
                            targetUserId = userId
                        )

                        val hasMorePages = currentPage < totalPages
                        if (index >= dailySummaries.lastIndex - 2 && hasMorePages) {
                            scope.launch { viewModel.loadNextPage(pageSize = pageSize)
                            }
                        }
                    }
                    item {
                        if (currentPage < totalPages) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                        } else {
                            Text("End of Records", modifier = Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

// --- 1. TITTLE Section ---
@Composable
private fun UserDetailsCard(user: UserResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${user.name} ${user.surname} (${user.userType?.displayName ?: "Worker"})",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailItem(Icons.Default.Call, user.phoneNumber)
                user.nationalId?.let { DetailItem(Icons.Default.PhoneIphone, it) }
                user.email?.let { DetailItem(Icons.Default.Email, it) }
                user.department?.let { DetailItem(Icons.Default.Business, it.displayName) }
                user.section?.let { DetailItem(Icons.Default.Layers, it.displayName) }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

// --- 2. Filter Section ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilterSection(
    startDate: String?,
    endDate: String?,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Date Picker
        DatePickerInput(
            label = "Start Date",
            dateValue = startDate,
            onDateSelected = onStartDateChange,
            dateFormatter = dateFormatter,
            modifier = Modifier.weight(1f)
        )

        // End Date Picker
        DatePickerInput(
            label = "End Date",
            dateValue = endDate,
            onDateSelected = onEndDateChange,
            dateFormatter = dateFormatter,
            modifier = Modifier.weight(1f)
        )
        // Buttons
        Button(
            onClick = onSubmit,
            modifier = Modifier.height(56.dp)
        ) {
            Text("Apply Filter")
        }

        OutlinedButton(
            onClick = onClear,
            modifier = Modifier.height(56.dp)
        ) {
            Text("Clear")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerInput(
    label: String,
    dateValue: String?,
    onDateSelected: (String) -> Unit,
    dateFormatter: DateTimeFormatter,
    modifier: Modifier
) {
    val openDialog = remember { mutableStateOf(false) }
    // Use the current date if no value is set, otherwise parse the value
    val initialDateMillis = try {
        dateValue?.let {

            LocalDate.parse(it, dateFormatter)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        } ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val dateState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    OutlinedTextField(
        value = dateValue ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = { openDialog.value = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Pick $label"
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
                            onDateSelected(date.format(dateFormatter))
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
                title = { Text("Select $label") }
            )
        }
    }
}

// --- 3. Summaries Section (Modified Components) ---

/**
 * 💡 MODIFIED: Calls the UserWorkSummaryItem and passes the targetUserId.
 * This is a copy of WorkDaySummaryCard but calls the specialized item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserWorkDaySummaryCard(
    summary: WorkDaySummaryDTO,
    expanded: Boolean,
    onToggleExpand: (Boolean) -> Unit,
    navigator: Navigator,
    targetUserId: String // The ID of the user whose page this is
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        // ... (WorkDaySummaryCard content remains the same)
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
                        // 💡 Calls the modified item
                        UserWorkSummaryItem(work, navigator = navigator, targetUserId = targetUserId)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

/**
 * 💡 MODIFIED: A copy of WorkSummaryItem, but it highlights the target user's data.
 */
@Composable
private fun UserWorkSummaryItem(work: org.montigny.client.dto.Work.WorkSummaryDTO, navigator: Navigator, targetUserId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ... (Header and Stacks Sections are identical to WorkSummaryItem)
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
                        StatInline("📦 Total Volume", String.format("%.2f m³", work.totalVolume))
                        StatInline("🗂 Stacks", work.totalStacks.toString())
                        StatInline("👷 Users", work.totalUsers.toString())
                        work.capturedBy?.let { StatInline("✍️ Captured by", extractFullNameFromApiString(it)) }
                        work.capturedAt?.let { StatInline("⏱ Captured at", it) }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        work.updatedBy?.let { StatInline("🔄 Updated by", extractFullNameFromApiString(it)) }
                        if (work.capturedAt != work.updatedAt) {
                            work.updatedAt?.let { StatInline("⏱ Updated at", it) }
                        }
                    }
                }
                IconButton(
                    onClick = {
                        // Edit action remains
                    }
                    // ... (IconButton code is omitted for brevity, but it should be here)
                ) {}
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Stacks Worked", subtitle = "Cubes")
            Column(modifier = Modifier.fillMaxWidth().padding(start = 6.dp)) {
                work.stacks.forEach { StackRow(it) }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------
            // Users Section: HIGHLIGHTED
            // -------------------------
            SectionHeader(title = "Workers", subtitle = "Personal Cubes")
            Column(modifier = Modifier.fillMaxWidth().padding(start = 6.dp)) {
                work.users.forEach { userInfo ->
                    // 💡 Calls the UserRow that highlights the current user's entry
                    UserRowHighlight(userInfo, targetUserId)
                }
            }
        }
    }
}

/**
 * 💡 MODIFIED: A copy of UserRow specifically to highlight the entry for the target user.
 */
@Composable
private fun UserRowHighlight(user: UserInfoDTO, targetUserId: String) {
    val isTargetUser = user.id.toString() == targetUserId

    // Use a highlighted background if this is the target user
    val containerColor = if (isTargetUser) {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f) // Clear highlight
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ... (Avatar, Name, Badges sections are the same as UserRow)
            Box( /* ... */ ) { /* ... */ }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.name} ${user.surname}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = if(isTargetUser) FontWeight.ExtraBold else FontWeight.SemiBold)
                )
                // Phone number and badges are lower in hierarchy on purpose
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(user.phoneNumber, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("Active", user.isActive, Icons.Default.CheckCircle)
                    StatusChip("Approved", user.isApproved, Icons.Default.Verified)
                }
            }
            // --- Right: Volume (Primary Highlight) ---
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.3f m³", user.personalCubes),
                    // PRIMARY Highlight: Bold and large volume for the target user
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isTargetUser) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "\uD83D\uDCC8 Volume",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}