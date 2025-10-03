package org.montigny.client.database

import at.favre.lib.crypto.bcrypt.BCrypt


class BCryptPasswordHasher : PasswordHasher {
    override fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    override fun verify(password: String, hashed: String): Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), hashed)

        println(result)

        return result.verified
    }
}
