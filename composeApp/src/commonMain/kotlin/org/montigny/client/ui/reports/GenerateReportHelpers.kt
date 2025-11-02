package org.montigny.client.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.montigny.client.ui.Theme.Danger
import org.montigny.client.util.Constants.Companion.SYSTEM_REPORTS_MAIN_PATH
import java.awt.Desktop
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Helper for date formatting
private const val DATE_FORMAT = "yyyy-MM-dd"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerSection(
    startDateMillis: Long?,
    onStartDateChange: (Long?) -> Unit,
    endDateMillis: Long?,
    onEndDateChange: (Long?) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DatePickerDefaults.dateFormatter() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start date field
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = startDateMillis?.let {
                    dateFormatter.formatDate(it, locale = Locale.getDefault())
                } ?: "",
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select Start Date")
                        }
                        IconButton(onClick = { onStartDateChange(null) }) {
                            Icon(Icons.Default.Clear,
                                tint  = Danger,
                                contentDescription = "Clear Start Date")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // End date field
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = endDateMillis?.let {
                    dateFormatter.formatDate(it, locale = Locale.getDefault())
                } ?: "",
                onValueChange = {},
                label = { Text("End Date") },
                readOnly = true,
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select End Date")
                        }
                        IconButton(onClick = { onEndDateChange(null) }) {
                            Icon(Icons.Default.Clear,
                                tint = Danger,
                                contentDescription = "Clear End Date")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Start date picker
    if (showStartDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onStartDateChange(pickerState.selectedDateMillis)
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    // End date picker
    if (showEndDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = endDateMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onEndDateChange(pickerState.selectedDateMillis)
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// Helper function for converting milliseconds to formatted date string
fun Long.toDateString(): String {
    return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(this))
}

// Function to open the system file explorer to a specific folder
fun openReportDirectory() {
    val reportsPath = System.getProperty("user.home") + File.separator + "$SYSTEM_REPORTS_MAIN_PATH"
    val file = File(reportsPath)
    if (file.exists() && file.isDirectory) {
        try {
            // Desktop-specific API to open the directory
            Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            println("failed to open system files folder")
            e.printStackTrace()
            // In a real application, you might show a toast/snackbar here
        }
    }
}
fun openOutputFile(filePath: String) {
    val file = File(filePath)
    if (file.exists() && file.isFile) {
        try {
            Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}