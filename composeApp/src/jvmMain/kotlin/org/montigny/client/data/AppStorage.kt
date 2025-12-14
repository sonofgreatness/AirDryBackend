package org.montigny.client.data

import java.util.prefs.Preferences

actual  class   AppStorage actual constructor() {
    private val prefs = Preferences.userRoot().node("org.montigny.client")

    actual fun put(key: String, value: String) {
        prefs.put(key, value)
    }

    actual fun get(key: String): String? {
        return prefs.get(key, null)
    }

    actual fun remove(key: String) {
        prefs.remove(key)
    }
}