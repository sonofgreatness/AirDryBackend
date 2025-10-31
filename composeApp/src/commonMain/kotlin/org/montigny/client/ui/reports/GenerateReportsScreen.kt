package org.montigny.client.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.montigny.client.ui.reports.viewModel.GenerateReportsViewModel
import org.montigny.client.ui.settings.stacks.StatusMessageCard

@Composable
fun GenerateReportsScreen(
    viewModel: GenerateReportsViewModel
) {
    val scrollState = rememberScrollState()
    // Messages
    var pdfMessage by remember { mutableStateOf<String?>(null) }
    var payRollMessage by remember { mutableStateOf<String?>(null) }
    var dashBoardMessage by remember { mutableStateOf<String?>(null) }

    // Observe StateFlows
    val pdfState by viewModel.pdfGeneratorMessage.collectAsState()
    val  payRollState by viewModel.payRollGeneratorMessage.collectAsState()
    val dashBoardState by viewModel.dashBoardGeneratorMessage.collectAsState()
    LaunchedEffect(pdfState) {
        pdfMessage = pdfState
        if (pdfState?.contains("Done", true) == true) {
            delay(3000)
            pdfMessage = null
        }
    }
    Scaffold() { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row {
            Text("Generate reports screen", modifier = Modifier.clickable {
                viewModel.exportStacksDataToPdf()
            })
        }
        Row {
            Text("Generate payroll screen", modifier = Modifier.clickable {
                viewModel.exportPayRollDataToExcel("","")
            })
        }

        Row {
            Text("Generate dashBoard screen", modifier = Modifier.clickable {
                viewModel.exportDashBoardToExcel("","")
            })
        }
    Row{
        Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        pdfMessage?.let { StatusMessageCard("PDF", it) }
        }
      payRollMessage?.let { StatusMessageCard("Pay roll", it) }
      dashBoardMessage?.let { StatusMessageCard("DashBoard", it) }
    }
    }
    Row{Text("Dummy Text")}
}
}