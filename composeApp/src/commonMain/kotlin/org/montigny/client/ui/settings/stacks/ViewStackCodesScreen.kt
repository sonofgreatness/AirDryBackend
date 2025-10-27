package org.montigny.client.ui.settings.stacks

import ColoredSvgIcon
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.settings.stacks.viewModel.StacksViewModel

@Composable
fun ViewStackCodesScreen(viewModel: StacksViewModel, navigator: Navigator) {
    val stacks by viewModel.allStacks.collectAsState()
    val stackCodes by viewModel.allStackCodes.collectAsState()
    val scrollState = rememberScrollState()
    var menuExpanded by remember { mutableStateOf(false) }

    // Messages
    var pdfMessage by remember { mutableStateOf<String?>(null) }
    var excelMessage by remember { mutableStateOf<String?>(null) }

    // Observe StateFlows
    val pdfState by viewModel.pdfGeneratorMessage.collectAsState()
    val excelState by viewModel.xslGeneratorMessage.collectAsState()

    // Auto-update messages
    LaunchedEffect(pdfState) {
        pdfMessage = pdfState
        if (pdfState?.contains("Done", true) == true) {
            delay(3000)
            pdfMessage = null
        }
    }
    LaunchedEffect(excelState) {
        excelMessage = excelState
        if (excelState?.contains("Done", true) == true) {
            delay(3000)
            excelMessage = null
        }
    }
    LaunchedEffect(stacks) {
        viewModel.populateAllStacksList()
    }
    fun loadSvgFromResources(resourcePath: String, density: Density = Density(1f)): Painter {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")
        return loadSvgPainter(stream, density)
    }

    val excelIcon = loadSvgFromResources("drawable/excel.svg")
    val pdfIcon = loadSvgFromResources("drawable/pdf.svg")

    LaunchedEffect(stacks) {
        viewModel.populateAllStacksList()
    }

    Scaffold(
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Export")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Export to Excel") },
                        leadingIcon = { ColoredSvgIcon(excelIcon, "Export to Excel") },
                        onClick = {
                            menuExpanded = false
                            viewModel.exportStacksDataToExcel()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Export to PDF") },
                        leadingIcon = {
                            Icon(
                                pdfIcon,
                                contentDescription = "PDF Icon",
                                tint = androidx.compose.ui.graphics.Color.Unspecified
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            viewModel.exportStacksDataToPdf()
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ----------------- STATUS MESSAGES -----------------
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pdfMessage?.let { StatusMessageCard("PDF", it) }
                excelMessage?.let { StatusMessageCard("Excel", it) }
            }
            // ----------------- STACK CODES -----------------
            Text(
                "Stack Codes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TableCell("Code", weight = 1f, bold = true)
                        TableCell("Display Name", weight = 2f, bold = true)
                    }
                    Divider()
                    stackCodes.forEach { code ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TableCell(code.displayCode ?: "", weight = 1f)
                            TableCell(code.displayName ?: "", weight = 2f)
                        }
                        Divider()
                    }
                }
            }

            // ----------------- STACKS -----------------
            Text(
                "Stacks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TableCell("Stack Name", weight = 2f, bold = true)
                        TableCell("Description", weight = 1f, bold = true)
                        TableCell("Code", weight = 1f, bold = true)
                        TableCell("Length", weight = 1f, bold = true)
                        TableCell("Height", weight = 1f, bold = true)
                        TableCell("Thickness", weight = 1f, bold = true)
                        TableCell("Pieces", weight = 1f, bold = true)
                        TableCell("Volume", weight = 1f, bold = true)
                    }
                    Divider()
                    stacks.forEach { stack ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TableCell(stack.displayName, weight = 2f)
                            TableCell(stack.stackCode.displayName ?: "", weight = 1f)
                            TableCell(stack.stackCode.displayCode ?: "", weight = 1f)
                            TableCell(stack.length?.let { String.format("%.3f", it) } ?: "-", weight = 1f)
                            TableCell(stack.height?.let { String.format("%.3f", it) } ?: "-", weight = 1f)
                            TableCell(stack.thickness?.let { String.format("%.3f", it) } ?: "-", weight = 1f)
                            TableCell(stack.numberOfPieces?.toString() ?: "-", weight = 1f)
                            TableCell(stack.volume?.let { String.format("%.3f", it) } ?: "-", weight = 1f)
                        }
                        Divider()
                    }
                }
            }


        }
    }
}

@Composable
fun StatusMessageCard(type: String, message: String) {
    val color = when {
        message.contains("Loading", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        message.contains("Done", ignoreCase = true) -> MaterialTheme.colorScheme.secondary
        message.contains("Failed", ignoreCase = true) -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$type Export:", fontWeight = FontWeight.Bold)
                Text(text = message, color = color, fontWeight = FontWeight.Medium)
            }

            if (message.contains("Loading", ignoreCase = true)) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = color
                )
            }
        }
    }
}

@Composable
fun RowScope.TableCell(text: String, weight: Float, bold: Boolean = false) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(8.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

