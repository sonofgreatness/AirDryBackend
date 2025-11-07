package org.montigny.client.ui.Theme

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(private val settings: Settings) {
    private val _isDarkThemeEnabled = MutableStateFlow(getTheme())

    private fun getTheme() : Boolean {
        val systemSetting = settings.getBoolean(THEME_KEY, false)
        return  systemSetting
    }
    init {
        _isDarkThemeEnabled.value = getTheme()
    }
    val isDarkThemeEnabled = _isDarkThemeEnabled.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        settings.putBoolean(THEME_KEY, enabled)
        val newSystemSetting = settings.getBoolean(THEME_KEY, false)
        _isDarkThemeEnabled.value = newSystemSetting
    }
}