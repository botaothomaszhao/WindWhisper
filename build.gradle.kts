val logback_version: String by project
val jline_version: String by project

plugins {
    kotlin("jvm") version "2.3.0-RC2"
    kotlin("plugin.serialization") version "2.3.0-RC2"
    id("io.ktor.plugin") version "3.3.2"
}

group = "moe.tachyon.windwhisper"
version = "1.0.0"

application {
    mainClass.set("moe.tachyon.windwhisper.MainKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect")) // kotlin 反射库

    // ktor client
    implementation("io.ktor:ktor-client-core-jvm") // core
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation") // request/response时反序列化

    // ktor common
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm") // json on request/response
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1") // json on request/response
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2") // 协程

    implementation("org.fusesource.jansi:jansi:2.4.1") // 终端颜色码
    implementation("org.jline:jline:$jline_version") // 终端打印、命令等
    implementation("ch.qos.logback:logback-classic:${logback_version}")  // 日志
    implementation("com.charleskorn.kaml:kaml:0.80.0") // yaml for kotlin on read/write file
    implementation("me.nullaqua:BluestarAPI-kotlin:4.3.7")
    implementation("me.nullaqua:BluestarAPI-kotlin-reflect:4.3.7")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
        freeCompilerArgs.add("-Xnested-type-aliases")
        freeCompilerArgs.add("-Xdata-flow-based-exhaustiveness")
        freeCompilerArgs.add("-Xallow-reified-type-in-catch")
        freeCompilerArgs.add("-Xallow-holdsin-contract")
    }
}

ktor {
    fatJar {
        allowZip64 = true
        archiveFileName = "WindWhisper.jar"
    }
}