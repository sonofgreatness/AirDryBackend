package org.montigny.client.ui.Theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun TimberTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) TimberDarkColors else TimberLightColors

    MaterialTheme(
        colorScheme = colors,
        typography = TimberTypography,
        content = content
    )
}