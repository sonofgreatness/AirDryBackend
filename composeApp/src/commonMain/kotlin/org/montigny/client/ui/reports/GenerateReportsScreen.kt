package org.montigny.client.ui.reports

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.montigny.client.ui.reports.viewModel.GenerateReportsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateReportsScreen(
    viewModel: GenerateReportsViewModel
) {
    val scrollState = rememberScrollState()
    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    val pdfState by viewModel.pdfGeneratorMessage.collectAsState()
    val payRollState by viewModel.payRollGeneratorMessage.collectAsState()
    val dashBoardState by viewModel.dashBoardGeneratorMessage.collectAsState()
    var pdfMessage by remember { mutableStateOf<Pair<String, String?>>(pdfState!! to null) } // Pair of (message, filePath)
    var payRollMessage by remember { mutableStateOf<Pair<String, String?>>(payRollState!! to null) }
    var dashBoardMessage by remember { mutableStateOf<Pair<String, String?>>(dashBoardState!! to null) }
    val isSyncingPayRoll = viewModel.isSyncingPayRoll.collectAsState()
    val isSyncingMasterData = viewModel.isSyncingMasterData.collectAsState()
    val isSyncingPdf = viewModel.isSyncingPdf.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.resetReportMessages()
    }
    LaunchedEffect(pdfState) {
        pdfMessage = parseFileMessage(pdfState)
    }
    LaunchedEffect(payRollState) {
        payRollMessage = parseFileMessage(payRollState)
    }
    LaunchedEffect(dashBoardState) {
        dashBoardMessage = parseFileMessage(dashBoardState)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Generation Module", fontWeight = FontWeight.Bold) },
                actions = {
                    OutlinedButton(onClick = { openReportDirectory() }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Open Reports Directory")
                        Spacer(Modifier.width(8.dp))
                        Text("Open Reports Folder")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            DateRangePickerSection(
                startDateMillis = startDateMillis,
                onStartDateChange = { startDateMillis = it },
                endDateMillis = endDateMillis,
                onEndDateChange = { endDateMillis = it }
            )
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            ReportGenerationCard(
                title = "Weekly Stacks Summary PDF",
                description = "Generates a comprehensive PDF report detailing stacks, percentages, and total cubes for all casual workers, grouped by department and section for the selected period. This report is essential for productivity and cubes tracking.",
                imageResourcePath = "drawable/pdf.svg",
                currentMessage = pdfMessage.first,
                filePath = pdfMessage.second,
                buttonText = "Generate PDF Report",
                onClick = {
                    val start = startDateMillis?.toDateString() ?: ""
                    val end = endDateMillis?.toDateString() ?: ""
                    viewModel.setStartAndEndDateFilters(start, end)
                    viewModel.exportStacksDataToPdf()
                },
                isSyncingPdf
            )
            ReportGenerationCard(
                title = "Payroll Data Excel Export",
                description = "Exports a detailed Excel file containing all payroll-relevant data (summary  of  personal cubes per worker per day) ",
                imageResourcePath = "drawable/excel.svg",
                currentMessage = payRollMessage.first,
                filePath = payRollMessage.second,
                buttonText = "Generate Payroll Excel",
                onClick = {
                    val start = startDateMillis?.toDateString() ?: ""
                    val end = endDateMillis?.toDateString() ?: ""
                    viewModel.exportPayRollDataToExcel(start, end)
                },
                isSyncing = isSyncingPayRoll
            )
            ReportGenerationCard(
                title = "Master Dashboard Data Excel",
                description = "Generates a raw data Excel file containing key performance indicators (KPIs) and aggregated metrics for the entire operation. This data is the source for executive-level business intelligence dashboards.",
                imageResourcePath = "drawable/excel.svg",
                currentMessage = dashBoardMessage.first,
                filePath = dashBoardMessage.second,
                buttonText = "Generate Dashboard Excel",
                onClick = {
                    val start = startDateMillis?.toDateString() ?: ""
                    val end = endDateMillis?.toDateString() ?: ""
                    viewModel.exportDashBoardToExcel(start, end)
                },
                isSyncing = isSyncingMasterData
            )
        }
    }
}
@Composable
fun ReportGenerationCard(
    title: String,
    description: String,
    imageResourcePath: String,
    currentMessage: String?,
    filePath: String?,
    buttonText: String,
    onClick: () -> Unit,
    isSyncing: State<Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onClick,
                        enabled = currentMessage?.contains("Loading", ignoreCase = true) == false
                                && currentMessage?.contains("Done", ignoreCase = true) != true
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(buttonText)
                    }
                }



                Card(
                    modifier = Modifier.weight(1f).height(150.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    // **REPLACED Box and Text with Image Composable**
                    Image(
                        painter = painterResource(imageResourcePath),
                        contentDescription = "Preview of $title",
                        // This scales the image to fit the bounds of the Card
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (isSyncing.value) {
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else{  currentMessage?.let {
                val message = if (filePath != null) "Success: File created at path shown below" else it
                StatusMessageCard(title.replace(" ", ""), message, filePath)
            }}
        }
    }
}
@Composable
fun StatusMessageCard(type: String, message: String, filePath: String?) {
    val displayMessage = when {
        filePath != null -> "Report Generated Successfully."
        message.contains("Loading", ignoreCase = true) -> "Processing..."
        message.contains("Done", ignoreCase = true) -> "Report Generated Successfully."
        message.contains("Failed", ignoreCase = true) || message.contains("error", ignoreCase = true) -> "Error: Failed to generate report."
        else -> message
    }
    val color = when {
        displayMessage.contains("Processing", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        displayMessage.contains("Successfully", ignoreCase = true) -> MaterialTheme.colorScheme.secondary
        displayMessage.contains("Error", ignoreCase = true) -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(text = displayMessage, color = color, fontWeight = FontWeight.Medium)
            }
            if (displayMessage.contains("Processing", ignoreCase = true)) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = color
                )
            }

            if (filePath != null || displayMessage.contains("Successfully", ignoreCase = true)) {
                val finalPath = filePath ?: "File path placeholder..." // Use actual path or placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "File: ${finalPath.substringAfterLast(File.separator)}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    Button(
                        onClick = { openOutputFile(finalPath) },
                        enabled = filePath != null
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open File")
                        Spacer(Modifier.width(8.dp))
                        Text("Open File")
                    }
                }
                Text(
                    text = "Location: $finalPath",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
private fun parseFileMessage(message: String?): Pair<String, String?> {
    if (message == null) return "" to null
    val lower = message.lowercase()
    return when {
        lower.endsWith(".pdf") || "weekly" in lower -> message to message
        lower.endsWith(".xlsx") || "payroll" in lower || "dashboard" in lower -> message to message
        else -> message to null
    }
}