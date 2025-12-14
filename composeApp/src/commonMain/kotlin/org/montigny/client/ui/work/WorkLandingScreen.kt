package org.montigny.client.ui.work

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkLandingScreen(navigator: Navigator) {
    // Scaffold provides the basic structure for the screen (TopAppBar, Content, etc.)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Work Management",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    ) { paddingValues ->
        // Content area with padding from the Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Align content to the top
        ) {

            // --- 1. Title/Header Section (More prominent) ---
            Text(
                text = "Manage Recorded Work Entries",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // --- 2. Primary Action: Record New Work (Highlighted) ---
            // Use an ElevatedCard to give the main action prominence
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Takes up 60% of the width
                    .height(100.dp)
                    .clickable { navigator.navigateTo(Destination.CreateWork) },
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Record New",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "RECORD NEW WORK",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- 3. Secondary Action: View Records (Standard Button) ---
            Button(
                onClick = { navigator.navigateTo(Destination.Work) },
                modifier = Modifier.fillMaxWidth(0.6f),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = "View Works",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "View All Recorded Works",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}