package org.montigny.client.ui.work
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.work.viewModel.CreateAndUpdateWorkViewModel
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import org.montigny.client.util.stateClasses.UiState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrUpdateWorkScreen(
    navigator: Navigator,
    viewModel: CreateAndUpdateWorkViewModel,
     isUpdate: Boolean = false,
     workId : String? = null,
) {
    val scope = rememberCoroutineScope()
    var currentPage by remember { mutableStateOf(1) }
    val pageSize = DEFAULT_PAGE_SIZE

    // --- Observed States ---
    val casualWorkers by viewModel.casualWorkers.collectAsState()
    val stacks by viewModel.stacks.collectAsState()
    val workerUiState by viewModel.workerUiState.collectAsState()
    val stackUiState by viewModel.stackUiState.collectAsState()

    // --- Selections ---
    val selectedWorkers = remember { mutableStateListOf<UUID>() }
    val selectedStacks = remember { mutableStateMapOf<UUID, Int>() }

    // --- UI State for Dialog ---
    var showConfirmDialog by remember { mutableStateOf(false) }

    // --- Query Fields ---
    var workerQuery by remember { mutableStateOf(TextFieldValue("")) }
    var stackQuery by remember { mutableStateOf(TextFieldValue("")) }

    // --- Initial Load ---
    LaunchedEffect(Unit) {
        viewModel.populateWorkers(page = currentPage, pageSize = pageSize)
        viewModel.populateStacks(page = currentPage, pageSize = pageSize)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Listen for UI Events from ViewModel ---
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is CreateAndUpdateWorkViewModel.WorkUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "DISMISS",
                        duration = SnackbarDuration.Short
                    )
                }
                is CreateAndUpdateWorkViewModel.WorkUiEvent.WorkSaved -> {
                    // Optional: Navigate back or clear selections after successful save
                    navigator.navigateTo(Destination.WorkLandingPage)
                }
                is CreateAndUpdateWorkViewModel.WorkUiEvent.WorkUpdated -> { // <-- Added WorkUpdated
                    // Navigate back to the work landing page after successful save/update
                    navigator.navigateTo(Destination.WorkLandingPage)
                }
            }
        }
    }
    LaunchedEffect(isUpdate, workId) {
        if (isUpdate && !workId.isNullOrEmpty()) {
            viewModel.loadWork(workId)
        }
    }

    // --- NEW: LaunchedEffect to populate selections from loaded work data ---
    LaunchedEffect(Unit) {
        viewModel.workDataToPopulate.collect { workDoneDTO ->
            // 1. Clear existing selections first
            selectedWorkers.clear()
            selectedStacks.clear()
            // 2. Populate selectedWorkers
            // The DTO structure has workersList: List<WorkDoneUsersDTO>
            // and each WorkDoneUsersDTO has userIds: List<UUID>
            workDoneDTO.workersList.flatMap { it.userIds }.distinct().forEach { userId ->
                selectedWorkers.add(userId)
            }
            // 3. Populate selectedStacks
            // The DTO structure has stacksList: List<WorkDoneStacksDTO>
            // We need to count the occurrences of each stackId
            workDoneDTO.stacksList.flatMap { it.stackIds }.forEach { stackId ->
                selectedStacks[stackId] = selectedStacks.getOrElse(stackId) { 0 } + 1
            }
            // Optional: Show a message that data has been loaded
            snackbarHostState.showSnackbar("Work data loaded for editing.", duration = SnackbarDuration.Short)
        }
    }

    if (showConfirmDialog) {
        WorkConfirmationDialog(
            selectedWorkerIds = selectedWorkers.toList(),
            selectedStackCounts = selectedStacks.toMap(),
            availableWorkers = casualWorkers,
            availableStacks = stacks,
            onDismiss = { showConfirmDialog = false },
            onConfirmSave = {
                val dto = CreateWorkDoneDTO(
                    // If updating, you MUST pass the original ID in the DTO
                    id = if (isUpdate && workId != null) workId.toUUID() else UUID.randomUUID(),
                    workDate = Date(),
                    workersList = if (selectedWorkers.isNotEmpty()) listOf(
                        CreateWorkDoneDTO.WorkDoneUsersDTO(
                            // The ID of the WorkDoneUsersDTO itself can be new or old,
                            // but the main WorkDoneDTO ID is what matters for update API call.
                            id = UUID.randomUUID(),
                            userIds = selectedWorkers.toList()
                        )
                    ) else emptyList(),
                    stacksList = selectedStacks.flatMap { (stackId, count) ->
                        List(count) {
                            CreateWorkDoneDTO.WorkDoneStacksDTO(
                                id = UUID.randomUUID(),
                                stackIds = listOf(stackId)
                            )
                        }
                    }
                )
                println("Generated DTO: $dto")

                // --- MODIFIED: Call Create or Update based on flag ---
                if (isUpdate && workId != null) {
                    viewModel.updateWork(workId, dto)
                } else {
                    viewModel.createWork(dto)
                }

                showConfirmDialog = false // Close dialog after action
            }
        )
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if(isUpdate)
                            "Create Work "
                        else
                            "Update Work",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
        ,
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 24.dp, vertical = 16.dp)) {

                /** 🔹 Search + Lists - Now in a clearer, divided layout **/
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    /** --- Workers Section (List Container) --- **/
                    WorkerListContainer(
                        modifier = Modifier.weight(1f),
                        workerQuery = workerQuery,
                        onWorkerQueryChange = {
                            workerQuery = it
                            viewModel.setWorkerQuery(it.text)
                        },
                        workerUiState = workerUiState,
                        casualWorkers = casualWorkers,
                        selectedWorkers = selectedWorkers,
                        onWorkerSelect = { worker ->
                            if (selectedWorkers.contains(worker.id)) {
                                selectedWorkers.remove(worker.id)
                            } else {
                                selectedWorkers.add(worker.id!!)
                            }
                        },
                        onLoadMore = {
                            scope.launch {
                                currentPage++
                                viewModel.populateWorkers(page = currentPage, pageSize = pageSize)
                            }
                        }
                    )

                    /** --- Stacks Section (List Container) --- **/
                    StackListContainer(
                        modifier = Modifier.weight(1f),
                        stackQuery = stackQuery,
                        onStackQueryChange = {
                            stackQuery = it
                            viewModel.setStackQuery(it.text)
                        },
                        stackUiState = stackUiState,
                        stacks = stacks,
                        selectedStacks = selectedStacks,
                        onCountChange = { stackId, count ->
                            if (count > 0) {
                                selectedStacks[stackId] = count
                            } else {
                                selectedStacks.remove(stackId)
                            }
                        },
                        onLoadMore = {
                            scope.launch {
                                currentPage++
                                viewModel.populateStacks(page = currentPage, pageSize = pageSize)
                            }
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                /** 🔹 Summary + Submit - Now a distinct Footer/Action area **/
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Summary is now inline and less emphasized than the action
                    val totalStacks = selectedStacks.values.sum()
                    Column {
                        Text(
                            "Selected Workers: ${selectedWorkers.size}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Total Stack Picks: $totalStacks",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Primary Action Button (UPDATED to show dialog)
                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = selectedWorkers.isNotEmpty() && selectedStacks.isNotEmpty(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Save Work")
                    }
                }
            }
        }
    )
}
// --- EXTRACTED COMPOSABLES FOR CLARITY AND REUSABILITY ---

@Composable
private fun WorkerListContainer(
    modifier: Modifier,
    workerQuery: TextFieldValue,
    onWorkerQueryChange: (TextFieldValue) -> Unit,
    workerUiState: UiState,
    casualWorkers: List<UserResponse>,
    selectedWorkers: List<UUID>,
    onWorkerSelect: (UserResponse) -> Unit,
    onLoadMore: () -> Unit
) {
    Column(modifier = modifier) {
        // Clearer title for the section
        Text("Workers Selection", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(Modifier.height(8.dp))

        // Search field remains an OutlinedTextField, which is fine for desktop
        OutlinedTextField(
            value = workerQuery,
            onValueChange = onWorkerQueryChange,
            label = { Text("Search Workers") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        // Card for visual separation, giving the list a distinct boundary
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            when (workerUiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
                    Text("Error loading workers", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(casualWorkers, key = { _, worker -> worker.id!! }) { index, worker ->
                            WorkerSelectableItemRefactored(
                                worker = worker,
                                isSelected = worker.id in selectedWorkers,
                                onSelect = { onWorkerSelect(worker) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                            // Load more logic
                            if (index >= casualWorkers.lastIndex - 2 && workerUiState !is UiState.Loading && workerUiState !is UiState.Error) {
                                onLoadMore()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StackListContainer(
    modifier: Modifier,
    stackQuery: TextFieldValue,
    onStackQueryChange: (TextFieldValue) -> Unit,
    stackUiState: UiState,
    stacks: List<GetStackDTO>,
    selectedStacks: MutableMap<UUID, Int>,
    onCountChange: (UUID, Int) -> Unit,
    onLoadMore: () -> Unit
) {
    Column(modifier = modifier) {
        Text("Stacks & Quantity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = stackQuery,
            onValueChange = onStackQueryChange,
            label = { Text("Search Stacks") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            when (stackUiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
                    Text("Error loading stacks", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(stacks, key = { _, stack -> stack.id!! }) { index, stack ->
                            StackSelectableItemRefactored(
                                stack = stack,
                                count = selectedStacks[stack.id] ?: 0,
                                onCountChange = { count -> onCountChange(stack.id!!, count) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                            // Load more logic
                            if (index >= stacks.lastIndex - 2 && stackUiState !is UiState.Loading && stackUiState !is UiState.Error) {
                                onLoadMore()
                            }
                        }
                    }
                }
            }
        }
    }
}

/** ✅ WORKER ITEM (Refactored for Desktop/Formal look) **/
@Composable
private fun WorkerSelectableItemRefactored(
    worker: UserResponse,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) // Subtle selection
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 8.dp), // Tighter padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Use a simple text column for the primary data
        Column(Modifier.weight(1f)) {
            Text(
                "${worker.name} ${worker.surname}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            // Use labelMedium for a smaller, cleaner secondary text
            Text(
                worker.phoneNumber,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Checkbox provides a clear visual selection state
        Checkbox(checked = isSelected, onCheckedChange = { onSelect() })
    }
}

/** ✅ STACK ITEM (Refactored to a clearer Numeric Input) **/
@Composable
private fun StackSelectableItemRefactored(
    stack: GetStackDTO,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    // The stack item now uses the standard surface color and focuses on a more formal input.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Tighter padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("${stack.printDimensionsAndVolume()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            // Use labelMedium for secondary info
            Text(stack.stackCode.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.width(16.dp))

        // Numeric Input Field with controls for a formal desktop look
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrease button
            IconButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                enabled = count > 0,
                modifier = Modifier.size(32.dp) // Smaller icon size for density
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease count")
            }

            // Formal TextField for the count, allowing direct input
            OutlinedTextField(
                value = if (count > 0) count.toString() else "", // Don't show "0" explicitly, but an empty field or use a hint
                onValueChange = { newValue ->
                    val newCount = newValue.toIntOrNull()
                    when {
                        newCount == null -> onCountChange(0) // Clear/invalid input -> 0
                        newCount >= 0 -> onCountChange(newCount)
                    }
                },
                label = { if (count == 0) Text("Qty") }, // Label/Hint when 0
                modifier = Modifier.width(60.dp), // Fixed size for clean alignment
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
                ,
                textStyle = MaterialTheme.typography.titleSmall.copy(textAlign = TextAlign.Center),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Increase button
            IconButton(
                onClick = { onCountChange(count + 1) },
                modifier = Modifier.size(32.dp) // Smaller icon size for density
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase count")
            }
        }
    }
}

// --- NEW CONFIRMATION DIALOG COMPOSABLE ---

@Composable
fun WorkConfirmationDialog(
    selectedWorkerIds: List<UUID>,
    selectedStackCounts: Map<UUID, Int>,
    availableWorkers: List<UserResponse>,
    availableStacks: List<GetStackDTO>,
    onDismiss: () -> Unit,
    onConfirmSave: () -> Unit
) {
    // 1. Map IDs to human-readable data
    val selectedWorkerNames = remember(selectedWorkerIds, availableWorkers) {
        availableWorkers
            .filter { it.id in selectedWorkerIds }
            .map { "${it.name} ${it.surname}" }
            .sorted()
    }

    val selectedStackDetails = remember(selectedStackCounts, availableStacks) {
        availableStacks
            .filter { it.id in selectedStackCounts.keys }
            .mapNotNull { stack ->
                val count = selectedStackCounts[stack.id]
                if (count != null && count > 0) {
                    "${stack.printDimensionsAndVolume()} x$count"
                } else null
            }
            .sorted()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Work Entry", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "Please review the work details before saving.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))

                // --- Workers Summary ---
                Text("Workers (${selectedWorkerNames.size}):", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Column {
                        if (selectedWorkerNames.isEmpty()) {
                            Text("No workers selected.")
                        } else {
                            selectedWorkerNames.forEach { name ->
                                Text("• $name", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // --- Stacks Summary ---
                val totalPicks = selectedStackCounts.values.sum()
                Text("Stacks Entered (Total: $totalPicks picks):", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Column {
                        if (selectedStackDetails.isEmpty()) {
                            Text("No stacks selected.")
                        } else {
                            selectedStackDetails.forEach { detail ->
                                Text("• $detail", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmSave) {
                Text("Confirm and Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}