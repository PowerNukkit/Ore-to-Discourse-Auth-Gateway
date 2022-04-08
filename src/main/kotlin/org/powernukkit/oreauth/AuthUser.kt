package org.powernukkit.oreauth

import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class AuthUser(
    val id: Long,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val lang: String?,
    val addGroups: String?
)
