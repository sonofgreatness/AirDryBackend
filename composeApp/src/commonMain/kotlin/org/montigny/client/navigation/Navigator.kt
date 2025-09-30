package org.montigny.client.navigation

import androidx.compose.runtime.*

class Navigator {
    private val _currentDestination = mutableStateOf<Destination>(Destination.Users)
    val currentDestination: State<Destination> = _currentDestination

    fun navigateTo(destination: Destination) {
        _currentDestination.value = destination
    }
}