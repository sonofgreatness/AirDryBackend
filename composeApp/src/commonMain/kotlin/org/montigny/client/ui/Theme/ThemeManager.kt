package org.montigny.client.ui.Theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager() {
    private val _isDarkThemeEnabled = MutableStateFlow(false)
    val isDarkThemeEnabled = _isDarkThemeEnabled.asStateFlow()

   fun setDarkTheme(enabled: Boolean) {
       _isDarkThemeEnabled.value = enabled
   }
}