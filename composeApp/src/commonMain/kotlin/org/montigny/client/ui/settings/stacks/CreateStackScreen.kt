package org.montigny.client.ui.settings.stacks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.settings.stacks.viewModel.StacksViewModel
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import org.montigny.client.util.stateClasses.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStackScreen(
    stackId: String? = null,
    viewModel: StacksViewModel,
    navigator: Navigator,
    isUpdate: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val uiState by if (!isUpdate)
         viewModel.createStackState.collectAsState()
     else
         viewModel.updateStackState.collectAsState()

    val  existingStack by viewModel.updateStack.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var thickness by remember { mutableStateOf("") }
    var numberOfPieces by remember { mutableStateOf("") }
    var stackCodeName by remember { mutableStateOf("") }
    var stackCodeDisplay by remember { mutableStateOf("") }
    // If edit mode, prepopulate
    LaunchedEffect(stackId) {
        if (isUpdate && stackId != null) {
            viewModel.resetUpdateState()
            viewModel.initializeStack(stackId.toUUID())
            existingStack?.let {
                displayName = it.displayName ?: ""
                length = it.length.toString() ?: ""
                height = it.height?.toString() ?: ""
                thickness = it.thickness?.toString() ?: ""
                numberOfPieces = it.numberOfPieces?.toString() ?: ""
                stackCodeName = it.stackCode.displayCode ?:""
                stackCodeDisplay = it.stackCode.displayName ?: ""
            }
        }
    }
    LaunchedEffect(isUpdate) {
        if(!isUpdate)
            viewModel.resetCreateState()
    }
    val displayNameError = displayName.isBlank()
    val stackCodeError = stackCodeDisplay.isBlank() || stackCodeName.isBlank()
    val lengthError = length.toFloatOrNull() == null
    val heightError = height.toFloatOrNull() == null
    val thicknessError = thickness.toFloatOrNull() == null
    val piecesError = numberOfPieces.toIntOrNull() == null


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val isWideScreen = maxWidth > 900.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // --- Header ---
            Text(
                if (!isUpdate) "Create Stack" else "Edit Stack",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Provide measurements and stack code details below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        StackCodeFields(
                            displayCode = stackCodeDisplay,
                            displayName = stackCodeName,
                            focusManager = focusManager,
                            onValueChange = { value, field ->
                                when (field) {
                                    "code" -> stackCodeDisplay = value
                                    "name" -> stackCodeName = value
                                }
                            },
                            hasError = stackCodeError
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        StackDetailsFields(
                            displayName = displayName,
                            length = length,
                            height = height,
                            thickness = thickness,
                            numberOfPieces = numberOfPieces,
                            focusManager = focusManager,
                            onValueChange = { value, field ->
                                when (field) {
                                    "displayName" -> displayName = value
                                    "length" -> length = value
                                    "height" -> height = value
                                    "thickness" -> thickness = value
                                    "pieces" -> numberOfPieces = value
                                }
                            },
                            displayNameError = displayNameError,
                            lengthError = lengthError,
                            heightError = heightError,
                            thicknessError = thicknessError,
                            piecesError = piecesError
                        )
                    }
                }
            } else {
                StackCodeFields(
                    displayCode = stackCodeDisplay,
                    displayName = stackCodeName,
                    focusManager = focusManager,
                    onValueChange = { value, field ->
                        when (field) {
                            "code" -> stackCodeDisplay = value
                            "name" -> stackCodeName = value
                        }
                    },
                    hasError = stackCodeError
                )
                Spacer(Modifier.height(24.dp))
                StackDetailsFields(
                    displayName = displayName,
                    length = length,
                    height = height,
                    thickness = thickness,
                    numberOfPieces = numberOfPieces,
                    focusManager = focusManager,
                    onValueChange = { value, field ->
                        when (field) {
                            "displayName" -> displayName = value
                            "length" -> length = value
                            "height" -> height = value
                            "thickness" -> thickness = value
                            "pieces" -> numberOfPieces = value
                        }
                    },
                    displayNameError = displayNameError,
                    lengthError = lengthError,
                    heightError = heightError,
                    thicknessError = thicknessError,
                    piecesError = piecesError
                )
            }

            Spacer(Modifier.height(32.dp))

            // --- Actions ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                         if(!isUpdate)
                                viewModel.createStack(
                                    displayName = displayName,
                                    length = length.toFloatOrNull(),
                                    height = height.toFloatOrNull(),
                                    thickness = thickness.toFloatOrNull(),
                                    numberOfPieces = numberOfPieces.toIntOrNull() ?: 1,
                                    displayCode = stackCodeDisplay,
                                    displayNameForStackCode = stackCodeName)
                        else
                             viewModel.updateStack(
                                 displayName = displayName,
                                 length = length.toFloatOrNull(),
                                 height = height.toFloatOrNull(),
                                 thickness = thickness.toFloatOrNull(),
                                 numberOfPieces = numberOfPieces.toIntOrNull() ?: 1,
                                 displayCode = stackCodeDisplay,
                                 displayNameForStackCode = stackCodeName)


                    },
                    enabled = !displayNameError &&
                            !stackCodeError &&
                            !lengthError &&
                            !heightError &&
                            !thicknessError &&
                            !piecesError &&
                            uiState !is UiState.Success
                ) {
                    if (uiState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (!isUpdate) "Creating..." else "Updating...")
                    } else {
                        Text(if (!isUpdate) "Create Stack" else "Update Stack")
                    }
                }

                OutlinedButton(onClick = { navigator.navigateTo(Destination.ManageStacksScreen) }) {
                    Text("Done")
                }
            }

            Spacer(Modifier.height(16.dp))

            when (uiState) {
                is UiState.Error -> Text(
                    (uiState as UiState.Error).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )

                is UiState.Success -> Text(
                    if (!isUpdate) "✅ Stack created successfully!"
                    else "✅ Stack updated successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                else -> {}
            }
        }
    }
}

// --- Stack Code Fields ---
@Composable
fun StackCodeFields(
    displayCode: String,
    displayName: String,
    focusManager: FocusManager,
    onValueChange: (String, String) -> Unit,
    hasError: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Stack Code Details", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = displayCode,
            onValueChange = { onValueChange(it, "code") },
            label = { Text("Display Code") },
            modifier = Modifier.fillMaxWidth(),
            isError = hasError && displayCode.isBlank(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            })
        )
        if (hasError && displayCode.isBlank())
            Text("Required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { onValueChange(it, "name") },
            label = { Text("Stack Code Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = hasError && displayName.isBlank(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            })
        )
        if (hasError && displayName.isBlank())
            Text("Required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
    }
}

// --- Stack Detail Fields ---
@Composable
fun StackDetailsFields(
    displayName: String,
    length: String,
    height: String,
    thickness: String,
    numberOfPieces: String,
    focusManager: FocusManager,
    onValueChange: (String, String) -> Unit,
    displayNameError: Boolean,
    lengthError: Boolean,
    heightError: Boolean,
    thicknessError: Boolean,
    piecesError: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Stack Details", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { onValueChange(it, "displayName") },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = displayNameError,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            })
        )
        if (displayNameError)
            Text("Required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = length,
            onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) onValueChange(it, "length") },
            label = { Text("Length (m)") },
            modifier = Modifier.fillMaxWidth(),
            isError = lengthError,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        if (lengthError)
            Text("Enter a valid number", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) onValueChange(it, "height") },
            label = { Text("Height (m)") },
            modifier = Modifier.fillMaxWidth(),
            isError = heightError,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        if (heightError)
            Text("Enter a valid number", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = thickness,
            onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) onValueChange(it, "thickness") },
            label = { Text("Thickness (m)") },
            modifier = Modifier.fillMaxWidth(),
            isError = thicknessError,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        if (thicknessError)
            Text("Enter a valid number", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = numberOfPieces,
            onValueChange = { if (it.matches(Regex("^\\d*\$"))) onValueChange(it, "pieces") },
            label = { Text("Number of Pieces") },
            modifier = Modifier.fillMaxWidth(),
            isError = piecesError,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
        if (piecesError)
            Text("Enter a valid integer", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
    }
}
