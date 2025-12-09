package moe.tachyon.windwhisper.forum

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import moe.tachyon.windwhisper.logger.WindWhisperLogger
import moe.tachyon.windwhisper.mainConfig

private val notificationLogger = WindWhisperLogger.getLogger()

@Serializable
data class NotificationUnreadPost(
    val topicId: Int,
    val notificationId: Long,
)

suspend fun LoginData.getUnreadPosts(): List<NotificationUnreadPost> = notificationLogger.warning("Failed to get unread posts")
{
    val res = get("${mainConfig.url}/notifications?limit=50&recent=false&bump_last_seen_reviewable=true")
    if (!res.status.isSuccess())
        error("Failed to get unread posts: ${res.status}\n${res.bodyAsText()}")
    return res.body<JsonObject>()["notifications"]!!
        .jsonArray.mapNotNull()
        { body ->
            if (body !is JsonObject) return@mapNotNull null
            if ((body["read"] as? JsonPrimitive)?.booleanOrNull == true ) return@mapNotNull null
            val topicId = (body["topic_id"] as? JsonPrimitive)?.intOrNull
            val notificationId = (body["id"] as? JsonPrimitive)?.longOrNull
            if (topicId == null || notificationId == null) return@mapNotNull null
            NotificationUnreadPost(topicId, notificationId)
        }
}.getOrNull() ?: emptyList()

suspend fun LoginData.markAsRead(notificationId: Long): Boolean = notificationLogger.warning("Failed to mark notification $notificationId as read")
{
    val res = put("${mainConfig.url}/notifications/mark-read")
    {
        setBody(
            FormDataContent(
                Parameters.build()
                {
                    append("id", notificationId.toString())
                }
            )
        )
    }
    if (!res.status.isSuccess())
        error("Failed to mark notification $notificationId as read: ${res.status}\n${res.bodyAsText()}")
    return res.status.isSuccess()
}.getOrNull() ?: false