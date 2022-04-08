package org.powernukkit.oreauth.routes

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.json.*
import org.powernukkit.oreauth.AuthUser
import org.powernukkit.oreauth.Settings
import org.slf4j.LoggerFactory
import kotlin.coroutines.cancellation.CancellationException

fun Routing.installUsersRoutes(settings: Settings, httpClient: HttpClient) {
    val log = LoggerFactory.getLogger(this::class.java)
    get("/api/users/{username}") {
        call.request.queryParameters["apiKey"].takeIf { it == settings.authApiKey } ?: run {
            call.respond(HttpStatusCode.Forbidden, "Forbidden")
            return@get
        }
        val username = call.parameters.getOrFail("username")
        try {
            val emailsAsync = async(Dispatchers.IO) {
                httpClient.request<JsonObject>(settings.discourseUrl) {
                    url.pathComponents("u", username, "emails.json")
                    headers.append("Api-Key", settings.discourseApiKey)
                    headers.append("Api-Username", settings.discourseApiUser)
                }
            }

            val jsonData = httpClient.request<JsonObject>(settings.discourseUrl) {
                url.pathComponents("u", "$username.json")
                headers.append("Api-Key", settings.discourseApiKey)
                headers.append("Api-Username", settings.discourseApiUser)
            }

            val userObj = jsonData["user"]!!.jsonObject
            call.respond(
                AuthUser(
                    id = userObj["id"]!!.jsonPrimitive.long,
                    username = userObj["username"]!!.jsonPrimitive.content,
                    email = emailsAsync.await()["email"]!!.jsonPrimitive.content,
                    avatarUrl = settings.discourseUrl + userObj["avatar_template"]!!.jsonPrimitive.content.replace("{size}", "256"),
                    lang = userObj["locale"]?.jsonPrimitive?.contentOrNull,
                    addGroups = userObj["groups"]?.jsonArray?.joinToString(separator = ",") { it.jsonObject["name"]!!.jsonPrimitive.content }
                )
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failed: Exception) {
            if (failed is ClientRequestException && failed.response.status == HttpStatusCode.NotFound) {
                call.respond(HttpStatusCode.NotFound, "")
            } else {
                log.error("Failed to call Sponge", failed)
                call.respond(HttpStatusCode.InternalServerError, "")
            }
        }
    }
}
