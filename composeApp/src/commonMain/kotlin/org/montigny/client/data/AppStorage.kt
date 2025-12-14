package org.montigny.client.data


expect class  AppStorage() {
    fun put(key: String, value: String)
    fun get(key: String): String?
    fun remove(key: String)
}