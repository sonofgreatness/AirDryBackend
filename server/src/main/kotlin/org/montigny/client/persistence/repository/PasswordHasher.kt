package org.montigny.client.persistence.repository

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, hashed: String): Boolean
}