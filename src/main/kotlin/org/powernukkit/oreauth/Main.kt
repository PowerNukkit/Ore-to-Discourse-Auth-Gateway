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

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.*
import kotlinx.cli.*
import org.powernukkit.oreauth.features.createHttpClient
import org.powernukkit.oreauth.features.installStatusPages
import org.powernukkit.oreauth.routes.*
import org.slf4j.LoggerFactory

object Main {
    private fun runServer(settings: Settings) = embeddedServer(CIO, port = settings.port, host = settings.host) {
        val httpClient = createHttpClient()

        install(ContentNegotiation) { json() }
        install(CallLogging)
        install(IgnoreTrailingSlash)
        installStatusPages(settings)
        routing {
            installSsoRoutes(settings)
            installLogoutRoutes(settings)
            installAvatarRoutes(settings)
            installRobotsTxt()
            installUsersRoutes(settings, httpClient)
            get("/favicon.ico") { call.respondRedirect(settings.discourseUrl + "/favicon.ico") }
            get("/accounts/settings/{username}") {
                val username = call.parameters.getOrFail("username")
                call.respondRedirect("${settings.discourseUrl}/u/$username/preferences")
            }
        }
    }.start(true)

    @JvmStatic
    fun main(args: Array<String>) {
        LoggerFactory.getLogger(Main::class.java).apply {
            info("\n"+("""
                    ---------------------------------------------------------------------------------
                    Copyright (C) 2022  José Roberto de Araújo Júnior <joserobjr@powernukkit.org>
                    This program comes with ABSOLUTELY NO WARRANTY
                    This is free software, and you are welcome to use and redistribute it under certain conditions.
                    Read the LICENSE file or visit https://www.gnu.org/licenses/agpl-3.0 for details.
                    ---------------------------------------------------------------------------------
            """.trimIndent()))
        }
        fun SingleNullableOption<String>.env(name: String) = System.getenv(name).takeUnless { it.isNullOrBlank() }.let { env ->
            if (env != null) {
                default(env)
            } else {
                required()
            }
        }

        fun SingleNullableOption<String>.env(name: String, fallback: String) = System.getenv(name).takeUnless { it.isNullOrBlank() }.let { env ->
            default(env ?: fallback)
        }

        fun SingleNullableOption<Int>.env(name: String, fallback: Int) = System.getenv(name).takeUnless { it.isNullOrBlank() }?.trim()?.toIntOrNull().let { env ->
            default(env ?: fallback)
        }

        val app = ArgParser("PowerNukkit Ore to Discourse Auth Gateway")
        val port by app.option(ArgType.Int, "port", "p", "The port where the server will run")
            .env("PORT", 8000)

        val host by app.option(ArgType.String, "host", "hs", "The host or IP where the server will run")
            .env("HOST", "0.0.0.0")

        val baseUrl by app.option(ArgType.String, "base-url", "bu", "The base URL where this auth gateway will be running")
            .env("BASE_URL", "http://localhost:8000")

        val oreUrl by app.option(ArgType.String, "ore-url", "ou", "The root URL of your Ore installation")
            .env("ORE_URL")

        val discourseUrl by app.option(ArgType.String, "discourse-url", "du", "The URL for the discourse homepage")
            .env("DISCOURSE_URL")

        val discourseSsoSecret by app.option(ArgType.String, "discourse-sso-secret", "dss", "The secret set at discourse connect provider secrets in the Discourse admin panel")
            .env("DISCOURSE_SSO_SECRET")

        val discourseApiKey by app.option(ArgType.String, "discourse-api-key", "dak", "The API key used to query and manipulate users")
            .env("DISCOURSE_API_KEY")

        val discourseApiUser by app.option(ArgType.String, "discourse-api-user", "dau", "The user which will be used with the discourse API key")
            .env("DISCOURSE_API_USER", "system")

        val authSsoSecret by app.option(ArgType.String, "auth-sso-secret", "ss", "The SSO Secret used to communicate with Ore")
            .env("AUTH_SSO_SECRET")

        val authApiKey by app.option(ArgType.String, "auth-api-key", "aak", "Key used when Ore tries to do some elevated actions on Discourse, like user management")
            .env("AUTH_API_KEY")

        app.parse(args)

        runServer(Settings(
            port = port,
            host = host,
            baseUrl = baseUrl.removeSuffix("/"),
            oreUrl = oreUrl.removeSuffix("/"),
            discourseUrl = discourseUrl,
            discourseSsoSecret = discourseSsoSecret,
            discourseApiKey = discourseApiKey,
            discourseApiUser = discourseApiUser,
            authSsoSecret = authSsoSecret,
            authApiKey = authApiKey,
        ))
    }
}
