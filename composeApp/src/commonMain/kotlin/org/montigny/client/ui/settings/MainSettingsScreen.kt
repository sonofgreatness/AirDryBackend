package org.montigny.client.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.montigny.client.domain.synchronization.SyncSettingsRepository
import org.montigny.client.ui.Theme.BrownDriftwood
import org.montigny.client.ui.Theme.GreenLight
import org.montigny.client.ui.Theme.GreenMain
import org.montigny.client.ui.Theme.GreenNile
import org.montigny.client.ui.Theme.Neutral
import org.montigny.client.ui.Theme.Success

/**
 * MainSettingsScreen
 *
 * @param darkThemeEnabled current theme flag (stateless)
 * @param onToggleTheme callback to toggle theme
 * @param initialSyncIntervalHours initial sync interval (1..24)
 * @param onSyncIntervalChange called when the user commits a new interval
 */
@Composable
fun MainSettingsScreen(
    darkThemeEnabled: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    initialSyncIntervalHours: Int,
    onSyncIntervalChange: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val min = 1f
    val max = 24f

    // Local slider state so dragging feels smooth and doesn't hammer settings
    var sliderValue by remember { mutableStateOf(initialSyncIntervalHours.coerceIn(1, 24).toFloat()) }
    // Keep local textual representation for immediate display
    val displayedHours = remember(sliderValue) { sliderValue.toInt() }

    // If the parent prop changes externally, update local state
    LaunchedEffect(initialSyncIntervalHours) {
        sliderValue = initialSyncIntervalHours.coerceIn(1, 24).toFloat()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            )

            // Appearance Section
            SettingSection(title = "Appearance") {
                SettingItem(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    description = "Use a darker color scheme for night-time work",
                    trailing = {
                        // Stateless switch (drives parent via callback)
                        Switch(
                            checked = darkThemeEnabled,
                            onCheckedChange = onToggleTheme,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GreenNile,
                                checkedTrackColor = GreenMain,
                                uncheckedThumbColor = BrownDriftwood
                            )
                        )
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // Data Sync Section
            SettingSection(title = "Data Sync") {
                SettingItem(
                    icon = Icons.Outlined.Sync,
                    title = "Sync Interval",
                    description = "How often app syncs in background (hours)"
                )

                Spacer(Modifier.height(8.dp))

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        // Commit when the user finishes dragging
                        val committed = sliderValue.toInt().coerceIn(1, 24)
                        onSyncIntervalChange(committed)
                    },
                    valueRange = min..max,
                    steps = (max - min).toInt() - 1,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = GreenNile,
                        activeTrackColor = GreenMain,
                        inactiveTrackColor = BrownDriftwood.copy(alpha = 0.4f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Every $displayedHours hour(s)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.width(12.dp))
                    // A small save button if you prefer explicit save
                    Button(onClick = {
                        val committed = sliderValue.toInt().coerceIn(1, 24)
                        onSyncIntervalChange(committed)
                    }) {
                        Text("Save")
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // System Section
            SettingSection(title = "System") {
                var notificationsEnabled by remember { mutableStateOf(true) } // you can lift state out later
                SettingItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Enable Notifications",
                    description = "Show desktop alerts for sync and system events",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Success,
                                checkedTrackColor = GreenLight,
                                uncheckedThumbColor = Neutral
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = GreenMain, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold,
                fontSize = 15.sp))
            Text(description, style = MaterialTheme.typography.labelMedium)
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}