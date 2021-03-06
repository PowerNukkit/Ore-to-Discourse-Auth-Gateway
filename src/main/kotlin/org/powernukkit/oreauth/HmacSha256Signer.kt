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

import io.ktor.sessions.*

@JvmInline
value class HmacSha256Signer(private val transformer: SessionTransportTransformerMessageAuthentication) {
    constructor(ssoKey: String): this(SessionTransportTransformerMessageAuthentication(ssoKey.toByteArray()))

    fun sign(payload: String): EncodedSso {
        val (signedPayload, signature) = transformer.transformWrite(payload).split('/', limit = 2)
        return EncodedSso(signedPayload, signature, this, true)
    }

    fun sign(parameters: SsoParameters): EncodedSso {
        return sign(parameters.payload)
    }

    fun verify(payload: String, sig: String): Boolean {
        val (expectedPayload, expectedSig) = sign(payload)
        return expectedPayload == payload && expectedSig == sig
    }
}
