package moe.tachyon.windwhisper

import kotlinx.serialization.Serializable

@Serializable
data class MainConfig(
    val url: String,
    val username: String,
    val password: String,
    val retry: Int,
    val defaultHeaders: Map<String, String>,
    val reactions: Map<String, String>,
    val webServer: WebServerConfig = WebServerConfig(),
)

@Serializable
data class WebServerConfig(
    val enabled: Boolean = false,
    val port: Int = 8080,
    val host: String = "0.0.0.0",
    val rootPath: String = "/"
)