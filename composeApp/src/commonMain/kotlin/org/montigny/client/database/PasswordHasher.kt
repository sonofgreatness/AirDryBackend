package org.montigny.client.database

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, hashed: String): Boolean
}