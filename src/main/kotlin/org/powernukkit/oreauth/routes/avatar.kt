/*
 * Copyright (C) 2022  José Roberto de Araújo Júnior <joserobjr@powernukkit.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.powernukkit.oreauth.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.powernukkit.oreauth.Settings

fun Routing.installAvatarRoutes(settings: Settings) {
    get("/avatar/{username}") {
        val username = call.parameters.getOrFail("username")
        val size = call.request.queryParameters["size"]
            ?.let {
                if ('x' !in it) it.toIntOrNull()
                else it.split('x', limit = 2).mapNotNull { v -> v.toIntOrNull() }.maxOrNull() }
            ?.coerceIn(1, 512) ?: 256
        val url = with(URLBuilder(settings.discourseUrl)) {
            pathComponents("user_avatar", host, username, size.toString(), "1.png")
            buildString()
        }
        call.respondRedirect(url)
    }
}
