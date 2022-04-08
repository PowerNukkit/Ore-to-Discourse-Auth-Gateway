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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"
    application
}

group = "org.powernukkit"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlinx("coroutines-core", "1.6.1"))
    implementation(kotlinx("serialization-json", "1.3.2"))
    implementation(kotlinx("cli", "0.3.4"))
    implementation(ktor("client-core"))
    implementation(ktor("client-cio"))
    implementation(ktor("client-serialization"))
    implementation(ktor("client-logging"))
    implementation(ktor("server-core"))
    implementation(ktor("server-cio"))
    implementation(ktor("serialization"))
    implementation("ch.qos.logback:logback-classic:1.2.11")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("org.powernukkit.oreauth.Main")
}

@Suppress("unused")
fun DependencyHandlerScope.ktor(module: String, version: String = "1.6.8") = "io.ktor:ktor-$module:$version"
@Suppress("unused")
fun DependencyHandlerScope.kotlinx(module: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$module:$version"
