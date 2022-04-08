package org.powernukkit.oreauth.features

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*

fun createHttpClient() = HttpClient(CIO) {
    install(Logging)
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}
