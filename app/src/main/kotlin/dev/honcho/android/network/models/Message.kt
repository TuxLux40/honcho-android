package dev.honcho.android.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "workspace_id") val workspaceId: String,
    @Json(name = "peer_id") val peerId: String? = null,
    val role: String,
    val content: String,
    @Json(name = "created_at") val createdAt: String? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class MessageCreate(
    @Json(name = "peer_id") val peerId: String? = null,
    val role: String,
    val content: String,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class MessageBatchCreate(
    val messages: List<MessageCreate>
)

@JsonClass(generateAdapter = true)
data class MessageUpdate(
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)
