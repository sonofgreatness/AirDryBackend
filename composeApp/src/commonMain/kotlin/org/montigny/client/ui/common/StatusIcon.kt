package org.montigny.client.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusDot(isOnline: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.size(10.dp)) {
        drawCircle(
            color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336), // green/red
            radius = size.minDimension / 2,
            center = Offset(size.width / 2, size.height / 2)
        )
    }
}
