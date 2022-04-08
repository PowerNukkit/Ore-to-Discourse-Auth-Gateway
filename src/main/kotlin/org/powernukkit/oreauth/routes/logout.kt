package org.powernukkit.oreauth.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.powernukkit.oreauth.Settings

fun Routing.installLogoutRoutes(settings: Settings) {
    route("/accounts") {
        get("/logout") {
            val referer = call.request.header("Referer")
            call.respondRedirect(with(URLBuilder(settings.discourseUrl)){
                if (!referer.isNullOrBlank()) {
                    parameters.append("referer", referer)
                }
                fragment = "action--ore-logout"
                buildString()
            })
        }
        get("/logout-ore") {
            val referer = call.request.header("Referer").takeUnless { it.isNullOrBlank() }
                ?: settings.discourseUrl
            call.respondRedirect(with(URLBuilder(settings.oreUrl)){
                pathComponents("kill-session")
                parameters.append("returnUrl", with(URLBuilder(referer)) {
                    if (fragment == "action--ore-logout") {
                        fragment = ""
                    }
                    buildString()
                })
                buildString()
            })
        }
    }
}
