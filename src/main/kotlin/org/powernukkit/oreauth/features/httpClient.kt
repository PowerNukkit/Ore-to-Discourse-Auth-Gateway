package org.powernukkit.oreauth.features

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import org.slf4j.LoggerFactory

object ClientLogger: HttpClientFeature<Unit, ClientLogger> {
    private val log = LoggerFactory.getLogger(ClientLogger::class.java)
    override val key = AttributeKey<ClientLogger>("MyLogger")
    override fun prepare(block: Unit.() -> Unit) = ClientLogger
    override fun install(feature: ClientLogger, scope: HttpClient) {
        scope.sendPipeline.intercept(HttpSendPipeline.Monitoring) {
            log.info("Call: ${context.method.value} ${Url(context.url)}")
        }
        scope.receivePipeline.intercept(HttpReceivePipeline.State) {
            log.info("Response: ${context.response.status} From: ${context.request.method.value} ${context.request.url}")
        }
    }
}

fun createHttpClient() = HttpClient(CIO) {
    install(ClientLogger)
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}
