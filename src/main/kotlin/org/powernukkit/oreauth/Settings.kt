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

package org.powernukkit.oreauth

@JvmRecord
data class Settings(
    val port: Int,
    val host: String,
    val baseUrl: String,
    val oreUrl: String,
    val discourseUrl: String,
    val discourseSsoSecret: String,
    val discourseApiKey: String,
    val discourseApiUser: String,
    val discourseOrgGroup: Int,
    val authSsoSecret: String,
    val authApiKey: String,
)
