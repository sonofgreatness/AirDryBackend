package org.montigny.client.ui.settings.stacks

import ColoredSvgIcon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.settings.stacks.viewModel.StacksViewModel
import java.util.Locale

@Composable
fun ViewStackCodesScreen(viewModel: StacksViewModel, navigator: Navigator) {
    val stacks by viewModel.allStacks.collectAsState()
    rememberScrollState()

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
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            if(pdfMessage==null && excelMessage==null) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Two cards per row, like an open book
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {

                    itemsIndexed (stacks) { index,stack ->
                        StackDetailsCard(stack = stack, onClick = {})
                    }
                }
            }else{
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    pdfMessage?.let { StatusMessageCard("PDF", it) }
                    excelMessage?.let { StatusMessageCard("Excel", it) }
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

@Composable
fun StackDetailsCard(stack: GetStackDTO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Card is clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Primary Identification (The "Title" of the Book)
            Text(
                text = stack.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Secondary Identification (The "Subtitle" or "Edition")
            Text(
                text = "${stack.stackCode.displayName} (${stack.stackCode.displayCode})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // --- Divider for visual break ---
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            // Key Metrics - Presented as a comfortable, readable list
            KeyMetricRow(label = "Pieces", value = stack.numberOfPieces.toString())
            KeyMetricRow(label = "Cubes (m³)", value = String.format(Locale.getDefault(), "%.3f", stack.volume ?: 0.0))

            Spacer(modifier = Modifier.height(8.dp))

            // Dimensions - Grouped together
            DimensionRow(
                label = "Dims",
                height = stack.height.toString(),
                length = stack.length.toString(),
                thickness = stack.thickness.toString()
            )
        }
    }
}
@Composable
fun KeyMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DimensionRow(label: String, height: String, length: String, thickness: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label (H x L x T)",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "$height x $length x $thickness",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}