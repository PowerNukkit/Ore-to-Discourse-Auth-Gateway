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

import io.ktor.http.*
import io.ktor.util.*

@Suppress("NOTHING_TO_INLINE")
@JvmInline
value class SsoParameters(val data: Parameters) {
    constructor(payload: String): this(payload.decodeBase64String().parseUrlEncodedParameters())
    constructor(returnUrl: Url, nonce: String): this(Parameters.build {
        append("return_sso_url", returnUrl.toString())
        append("nonce", nonce)
    })

    inline val returnUrl get() = Url(this["return_sso_url"])
    inline val nonce get() = this["nonce"]

    val payload: String get() = data.formUrlEncode().encodeBase64String()

    inline operator fun get(param: String) = data.getOrFail(param)
}
