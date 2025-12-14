import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun ColoredSvgIcon(painter: Painter, contentDescription: String) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        tint = androidx.compose.ui.graphics.Color.Unspecified // keeps original colors
    )
}