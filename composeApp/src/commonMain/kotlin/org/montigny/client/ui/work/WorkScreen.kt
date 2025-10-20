package org.montigny.client.ui.work

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE

@Composable
fun WorkScreen(
    workViewModel: WorkViewModel
) {
    val scope = rememberCoroutineScope()
    val workSummaries by workViewModel.dailyWorkSummaries.collectAsState()

    var currentPage by remember { mutableStateOf(1) }
    val pageSize = DEFAULT_PAGE_SIZE

    // track expanded day ids (dayLabel is unique-ish for this view)
    val expandedDays = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        workViewModel.populateDailyWorkSummaries(page = currentPage, pageSize = pageSize)
    }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                HeaderSection()

                Spacer(Modifier.height(18.dp))

                FilterSection() // placeholder filters

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

                            // lazy loading trigger: request next page when approaching end
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


@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Daily Work Records",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold)
        )

        // Placeholder toggle (wire up later)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Include deactivated users", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp))
            Switch(checked = false, onCheckedChange = null) // set onCheckedChange later
        }
    }
}

@Composable
private fun FilterSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Date") }, modifier = Modifier.weight(1f))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Department") }, modifier = Modifier.weight(1f))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Section") }, modifier = Modifier.weight(1f))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("User") }, modifier = Modifier.weight(1f))
    }
}

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
            // Top row: Day label + summary metrics + expand toggle
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(summary.dayLabel, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        StatInline("Volume (total Cubes)", String.format("%.2f m³", summary.totalVolume))
                        StatInline("Stacks", summary.totalStacks.toString())
                        StatInline("Users", summary.totalUsers.toString())
                    }
                }

                TextButton(onClick = { onToggleExpand(!expanded) }) {
                    Text(if (expanded) "Collapse" else "Expand")
                }
            }

            // Expanded section: list of WorkSummaryDTOs
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

@Composable
private fun StatInline(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(end = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun WorkSummaryItem(work: WorkSummaryDTO) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // Header for this work record
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(work.workDate, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    StatInline("Volume (total Cubes)", String.format("%.2f m³", work.totalVolume))
                    StatInline("Stacks", work.totalStacks.toString())
                    StatInline("Users", work.totalUsers.toString())
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Stacks table-like list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text("Stacks Worked", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Text("Cubes", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
        Column(modifier = Modifier.fillMaxWidth().padding(start = 6.dp)) {
            work.stacks.forEach { stack ->
                StackRow(stack)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween){
            Text("Workers", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Text("Personal cubes", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
        Column(modifier = Modifier.fillMaxWidth().padding(start = 6.dp)) {
            work.users.forEach { user ->
                UserRow(user)
            }
        }
    }
}

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
