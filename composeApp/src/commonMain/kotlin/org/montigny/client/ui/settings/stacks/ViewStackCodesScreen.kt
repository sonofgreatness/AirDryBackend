package org.montigny.client.ui.settings.stacks

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.montigny.client.navigation.Navigator
import org.montigny.client.ui.settings.stacks.viewModel.StacksViewModel

@Composable
fun ViewStackCodesScreen(viewModel: StacksViewModel, navigator: Navigator) {

    val stacks by viewModel.allStacks.collectAsState()
    val stackCodes by viewModel.allStackCodes.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val isOnlineLoggedIn by viewModel.isOnlineLoggedIn.collectAsState()
    LaunchedEffect(isNetworkAvailable) {
        viewModel.populateAllStacksList()
    }
    for (stackCode in stackCodes) {
        Text("$stackCode")
    }
}