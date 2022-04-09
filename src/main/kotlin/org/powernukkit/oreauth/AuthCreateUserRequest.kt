package org.powernukkit.oreauth

@JvmRecord
data class AuthCreateUserRequest (
    val username: String,
    val email: String,
    val verified: Boolean,
    val dummy: Boolean,
    val password: String,
)
