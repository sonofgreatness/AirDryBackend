package org.montigny.client.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment

@Composable
fun MainSettingsScreen() {
    Box(Modifier.fillMaxSize()) {
        Text("Main Settings",
            Modifier.align(Alignment.Center))
    }
}