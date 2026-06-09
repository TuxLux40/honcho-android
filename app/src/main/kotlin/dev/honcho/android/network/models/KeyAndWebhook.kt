package dev.honcho.android.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Key(
    val key: String,
    @Json(name = "key_id") val keyId: String? = null,
    val scope: String? = null
)

@JsonClass(generateAdapter = true)
data class KeyCreate(
    @Json(name = "workspace_id") val workspaceId: String? = null,
    @Json(name = "peer_id") val peerId: String? = null,
    @Json(name = "session_id") val sessionId: String? = null
)

@JsonClass(generateAdapter = true)
data class Webhook(
    @Json(name = "webhook_id") val webhookId: String,
    @Json(name = "workspace_id") val workspaceId: String,
    val url: String,
    val events: List<String>? = null,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class WebhookCreate(
    val url: String,
    val events: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class WebhookTestResult(
    val success: Boolean? = null,
    val message: String? = null
)
