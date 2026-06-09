package dev.honcho.android.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Workspace(
    @Json(name = "workspace_id") val workspaceId: String,
    val name: String? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class WorkspaceUpsert(
    @Json(name = "workspace_id") val workspaceId: String,
    val name: String? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class WorkspaceUpdate(
    val name: String? = null,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class QueueStatus(
    val status: String? = null,
    val pending: Int? = null,
    val processing: Int? = null
)
