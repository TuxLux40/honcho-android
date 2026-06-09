package dev.honcho.android.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PagedResponse<T>(
    val items: List<T> = emptyList(),
    val total: Int? = null,
    val page: Int? = null,
    @Json(name = "page_size") val pageSize: Int? = null,
    @Json(name = "has_more") val hasMore: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ListRequest(
    val page: Int = 1,
    @Json(name = "page_size") val pageSize: Int = 20,
    val filter: Map<String, @JvmSuppressWildcards Any>? = null
)

@JsonClass(generateAdapter = true)
data class SearchRequest(
    val query: String,
    @Json(name = "top_k") val topK: Int = 10
)
