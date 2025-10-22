package org.montigny.client.ui.work

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.montigny.client.navigation.Destination
import org.montigny.client.navigation.Navigator

@Composable
fun  WorkLandingScreen(navigator: Navigator) {

    Column{
        //Title
        Row {
            Text("Manage  Recorded Work here")
        }
Row {
    Button(
        onClick = { navigator.navigateTo(Destination.Work) }){ Text("View all recorded works") } }
        Button(
            onClick = {
                navigator.navigateTo(Destination.CreateWork)
            }){
            Text("Record new")
        }}
}