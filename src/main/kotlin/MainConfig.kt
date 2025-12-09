package moe.tachyon.windwhisper

import kotlinx.serialization.Serializable

@Serializable
data class MainConfig(
    val url: String,
    val username: String,
    val password: String,
    val retry: Int,
    val defaultHeaders: Map<String, String>,
)
{
}