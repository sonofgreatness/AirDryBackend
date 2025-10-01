package org.montigny.client.ui.users


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.koinInject
import org.montigny.client.ui.users.viewModels.UserViewModel

@Composable
fun UsersScreen(userViewModel: UserViewModel
) {
    val usersTypes  by userViewModel.useTypes.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Users Screen  size  => ${usersTypes.size} : raw value : \n $usersTypes")
        Button(

            onClick = {
                userViewModel.populateUserTypes()
            },
        )
        {
            Text("o")
        }

    }
}