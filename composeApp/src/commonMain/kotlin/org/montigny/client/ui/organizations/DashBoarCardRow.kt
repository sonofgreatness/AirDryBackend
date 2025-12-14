package org.montigny.client.ui.organizations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.montigny.client.ui.Theme.Info

@Composable
fun DashboardCard(title: String, count: Int, icon: ImageVector) {
    Card(
        modifier = Modifier
            .padding(12.dp)
            .size(160.dp),
        shape = CircleShape,
        border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.displaySmall.copy(color = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun DashboardCardWithAction(title: String, count: Int, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(12.dp)
            .size(160.dp).clickable(onClick = onClick),
        shape = CircleShape,
        border = BorderStroke(4.dp, Info),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Info, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.displaySmall.copy(color = Info)
            )
        }
    }
}

