package dev.honcho.android.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Session(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "workspace_id") val workspaceId: String,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SessionUpsert(
    @Json(name = "session_id") val sessionId: String? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class SessionUpdate(
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null,
    @Json(name = "is_active") val isActive: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class SessionCloneRequest(
    @Json(name = "to_session_id") val toSessionId: String? = null,
    @Json(name = "message_id") val messageId: String? = null
)

@JsonClass(generateAdapter = true)
data class SessionPeersUpdate(
    @Json(name = "peer_ids") val peerIds: List<String>
)

@JsonClass(generateAdapter = true)
data class SessionPeerConfig(
    val observe: Boolean? = null,
    val respond: Boolean? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class SessionContext(
    val context: String? = null,
    val summaries: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class SessionSummaries(
    val summaries: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class SearchResult(
    @Json(name = "message_id") val messageId: String? = null,
    val content: String? = null,
    val score: Double? = null
)
