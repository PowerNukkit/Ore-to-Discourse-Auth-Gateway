package org.powernukkit.oreauth

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class DiscourseCreateUserRequest (
    val name: String,
    val email: String,
    val password: String,
    val username: String,
    val active: Boolean,
    val approved: Boolean,
)
