package org.montigny.client

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform