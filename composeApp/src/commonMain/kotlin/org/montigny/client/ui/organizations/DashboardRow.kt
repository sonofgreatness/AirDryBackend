package org.montigny.client.ui.organizations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.montigny.client.ui.users.DashboardCard

@Composable
fun DashboardRow(orgCount: Int, deptCount: Int, sectCount: Int) {
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DashboardCard("Organizations", orgCount, icon = { Icon(Icons.Default.Domain, null) })
            DashboardCard("Departments", deptCount, icon = { Icon(Icons.Default.AccountTree, null) })
            DashboardCard("Sections", sectCount, icon = { Icon(Icons.Default.ViewList, null) })
        }
    }
}
