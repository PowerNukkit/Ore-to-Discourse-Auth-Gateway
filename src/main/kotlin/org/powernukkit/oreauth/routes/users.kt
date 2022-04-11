package org.powernukkit.oreauth.routes

import io.github.reactivecircus.cache4k.Cache
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.powernukkit.oreauth.AuthCreateUserRequest
import org.powernukkit.oreauth.AuthUser
import org.powernukkit.oreauth.DiscourseCreateUserRequest
import org.powernukkit.oreauth.Settings
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Routing.installUsersRoutes(settings: Settings, httpClient: HttpClient) {
    val log = LoggerFactory.getLogger(this::class.java)

    val userSearchCache = Cache.Builder()
        .expireAfterWrite(5.minutes)
        .maximumCacheSize(1000)
        .build<String, Optional<AuthUser>>()

    suspend fun getAuthUserByUsername(username: String): AuthUser = withContext(Dispatchers.IO) {
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
        AuthUser(
            id = userObj["id"]!!.jsonPrimitive.long,
            username = userObj["username"]!!.jsonPrimitive.content,
            avatarUrl = settings.discourseUrl + userObj["avatar_template"]!!.jsonPrimitive.content.replace("{size}", "256"),
            lang = userObj["locale"]?.jsonPrimitive?.contentOrNull,
            addGroups = userObj["groups"]?.jsonArray?.joinToString(separator = ",") { it.jsonObject["name"]!!.jsonPrimitive.content },
            email = emailsAsync.await()["email"]!!.jsonPrimitive.content
        )
    }

    post("/api/users") {
        val formParams = call.receiveParameters()
        if (formParams["api-key"] != settings.authApiKey) {
            call.respond(HttpStatusCode.Forbidden, "Forbidden")
            return@post
        }
        val receivedRequest = AuthCreateUserRequest(
            username = formParams.getOrFail("username"),
            email = formParams.getOrFail("email"),
            verified = formParams.getOrFail("verified").toBooleanStrict(),
            dummy = formParams.getOrFail("dummy").toBooleanStrict(),
            password = formParams["password"] ?: UUID.randomUUID().toString(),
        )
        require(receivedRequest.dummy) {
            "Only dummy requests are supported"
        }
        val userCreationResponse = httpClient.request<JsonObject>(settings.discourseUrl) {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            body = DiscourseCreateUserRequest(
                name = receivedRequest.username,
                username = receivedRequest.username,
                email = receivedRequest.email,
                password = UUID.randomUUID().toString(),
                active = true,
                approved = true,
            )
            url.pathComponents("users.json")
            headers.append("Api-Key", settings.discourseApiKey)
            headers.append("Api-Username", settings.discourseApiUser)
        }
        if (userCreationResponse["success"]?.jsonPrimitive?.booleanOrNull != true) {
            call.respond(HttpStatusCode.UnprocessableEntity)
            return@post
        }
        if (userCreationResponse["user_id"]!!.jsonPrimitive.long <= 0) {
            call.respond(HttpStatusCode.UnprocessableEntity)
            return@post
        }

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    httpClient.request<JsonObject>(settings.discourseUrl) {
                        method = HttpMethod.Post
                        body = JsonObject(mapOf("usernames" to JsonPrimitive(receivedRequest.username)))
                        url.pathComponents("groups", settings.discourseOrgGroup.toString(), "members.json")
                        headers.append("Api-Key", settings.discourseApiKey)
                        headers.append("Api-Username", settings.discourseApiUser)
                    }
                    break
                } catch (error: ClientRequestException) {
                    if (error.response.status != HttpStatusCode.TooManyRequests) {
                        throw error
                    }
                    try {
                        val details = error.response.receive<JsonObject>()
                        require(details["error_type"]!!.jsonPrimitive.content == "rate_limit")
                        val waitTime = details["extras"]!!.jsonObject["wait_seconds"]!!.jsonPrimitive.long.toDuration(DurationUnit.SECONDS)
                        delay(waitTime + 2.seconds)
                    } catch (cancellation: CancellationException) {
                        cancellation.addSuppressed(error)
                        throw cancellation
                    } catch (other: Exception) {
                        error.addSuppressed(other)
                        throw error
                    }
                }
            }
        }

        call.respond(
            getAuthUserByUsername(receivedRequest.username).also {
                userSearchCache.put(receivedRequest.username, Optional.of(it))
            }
        )
    }
    get("/api/users/{username}") {
        call.request.queryParameters["apiKey"].takeIf { it == settings.authApiKey } ?: run {
            call.respond(HttpStatusCode.Forbidden, "Forbidden")
            return@get
        }
        val username = call.parameters.getOrFail("username")
        try {
            userSearchCache.get(username)?.let { cached ->
                if (cached.isEmpty) {
                    call.respond(HttpStatusCode.NotFound, "Not Found")
                } else {
                    call.respond(cached.get())
                }
                return@get
            }

            val userData = getAuthUserByUsername(username)
            userSearchCache.put(username, Optional.of(userData))
            call.respond(userData)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failed: Exception) {
            if (failed is ClientRequestException && failed.response.status == HttpStatusCode.NotFound) {
                userSearchCache.put(username, Optional.empty())
                call.respond(HttpStatusCode.NotFound, "Not Found")
            } else {
                log.error("Failed to call Sponge", failed)
                call.respond(HttpStatusCode.InternalServerError, "")
            }
        }
    }
}
