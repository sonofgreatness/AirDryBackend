package org.montigny.client.navigation

import androidx.compose.runtime.*

class Navigator {
    private val _currentDestination = mutableStateOf<Destination>(Destination.Users)
    val currentDestination: State<Destination> = _currentDestination

    private val  _nextDestination = mutableStateOf<Destination?>(null)
    val nextDestination: State<Destination?> = _nextDestination

    fun navigateTo(destination: Destination, nextDestination_: Destination? = null) {
        _currentDestination.value = destination

        if(nextDestination_ != null && nextDestination_ != destination) {
            _nextDestination.value = nextDestination_
        }
    }
}