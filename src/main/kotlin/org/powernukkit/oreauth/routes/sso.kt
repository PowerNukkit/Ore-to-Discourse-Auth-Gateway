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
import io.ktor.util.pipeline.*
import org.powernukkit.oreauth.EncodedSso
import org.powernukkit.oreauth.HmacSha256Signer
import org.powernukkit.oreauth.Settings
import org.powernukkit.oreauth.SsoParameters

fun Routing.installSsoRoutes(settings: Settings) {
    val authSigner = HmacSha256Signer(settings.authSsoSecret)
    val discourseSigner = HmacSha256Signer(settings.discourseSsoSecret)
    val discourseUrl = settings.discourseUrl

    route("/sso") {
        head { call.respond("") }
        suspend fun PipelineContext<*, ApplicationCall>.loginProcess() {
            val authSsoRequest = try {
                readEncodedSso(authSigner).also { require(it.isValid); }
            } catch (_: Exception) {
                call.respond(HttpStatusCode.Forbidden, "Login Error")
                return
            }
            val authRequest = SsoParameters(
                returnUrl = Url("${settings.baseUrl}/sso/discourse-callback/${authSsoRequest.payload}/${authSsoRequest.signature}"),
                nonce = authSsoRequest.parameters.nonce
            )
            val discourseSsoRequest = discourseSigner.sign(authRequest.payload)
            call.respondRedirect("$discourseUrl/session/sso_provider?sso=${discourseSsoRequest.payload}&sig=${discourseSsoRequest.signature}")
        }
        get {
            loginProcess()
        }
        get("/signup") {
            loginProcess()
        }
        get("/sudo") {
            loginProcess()
        }
        get("discourse-callback/{payload}/{signature}") {
            val authSsoRequest = try {
                EncodedSso(
                    payload = call.parameters.getOrFail("payload"),
                    signature = call.parameters.getOrFail("signature"),
                    signer = authSigner,
                ).also { require(it.isValid) }
            } catch (_: Exception) {
                call.respond(HttpStatusCode.Forbidden, "Login Error")
                return@get
            }
            val discourseSsoResponse = readEncodedSso(discourseSigner)
            val userData = discourseSsoResponse.parameters.data.filter { key, _ -> key != "return_sso_url" && key != "groups" }
            val responseSso = SsoParameters(Parameters.build {
                appendAll(userData)
                append("language", "en")
                append("return_sso_url", authSsoRequest.parameters["return_sso_url"])
                append("add_groups", discourseSsoResponse.parameters["groups"])
            })
            val encodedResponse = authSigner.sign(responseSso)
            val returnUrl = responseSso.returnUrl
            val signedUrl = returnUrl.copy(parameters = Parameters.build {
                appendAll(returnUrl.parameters.filter { key, _ -> key != "sig" && key != "sso" })
                append("sso", encodedResponse.payload)
                append("sig", encodedResponse.signature)
            }).toString()
            call.respondRedirect(signedUrl)
        }
    }
}

private fun PipelineContext<*, ApplicationCall>.readEncodedSso(signer: HmacSha256Signer): EncodedSso {
    val payload = call.request.queryParameters.getOrFail("sso")
    val signature = call.request.queryParameters.getOrFail("sig")
    return EncodedSso(payload, signature, signer)
}
