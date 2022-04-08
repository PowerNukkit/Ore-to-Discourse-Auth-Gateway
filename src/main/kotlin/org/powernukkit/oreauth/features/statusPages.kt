package org.powernukkit.oreauth.features

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import org.powernukkit.oreauth.Settings

fun Application.installStatusPages(settings: Settings) {
    install(StatusPages) {
        exception<NotFoundException> {
            call.respondRedirect(settings.oreUrl + "/404.html")
        }
        exception<BadRequestException> {
            call.respondRedirect(settings.oreUrl + "/400.html")
        }
        exception<Throwable> {
            call.respondRedirect(settings.oreUrl + "/500.html")
        }
        status(HttpStatusCode.NotFound) {
            call.respondRedirect(settings.oreUrl + "/404.html")
        }
    }
}
