package dev.honcho.android.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Conclusion(
    @Json(name = "conclusion_id") val conclusionId: String,
    @Json(name = "workspace_id") val workspaceId: String,
    @Json(name = "peer_id") val peerId: String? = null,
    val content: String,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ConclusionCreate(
    @Json(name = "peer_id") val peerId: String? = null,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ConclusionBatchCreate(
    val conclusions: List<ConclusionCreate>
)

@JsonClass(generateAdapter = true)
data class ConclusionQueryRequest(
    val query: String,
    @Json(name = "top_k") val topK: Int = 10,
    @Json(name = "peer_id") val peerId: String? = null
)
