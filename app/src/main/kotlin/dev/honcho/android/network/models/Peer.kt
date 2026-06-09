package dev.honcho.android.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Peer(
    @Json(name = "peer_id") val peerId: String,
    @Json(name = "workspace_id") val workspaceId: String,
    val name: String? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class PeerUpsert(
    @Json(name = "peer_id") val peerId: String,
    val name: String? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class PeerUpdate(
    val name: String? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class PeerCard(
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class PeerCardUpdate(
    val content: String
)

@JsonClass(generateAdapter = true)
data class PeerChatRequest(
    val query: String,
    val options: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class PeerRepresentationRequest(
    val queries: List<String>? = null,
    val options: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class PeerRepresentation(
    val content: String? = null,
    val facts: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class PeerContext(
    val context: String? = null,
    val facts: List<String>? = null
)
