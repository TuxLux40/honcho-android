package dev.honcho.android.network

import dev.honcho.android.network.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface HonchoService {

    // ===== WORKSPACES =====

    @POST("v3/workspaces")
    suspend fun getOrCreateWorkspace(@Body req: WorkspaceUpsert): Workspace

    @POST("v3/workspaces/list")
    suspend fun listWorkspaces(@Body req: ListRequest): PagedResponse<Workspace>

    @PUT("v3/workspaces/{workspaceId}")
    suspend fun updateWorkspace(
        @Path("workspaceId") workspaceId: String,
        @Body req: WorkspaceUpdate
    ): Workspace

    @DELETE("v3/workspaces/{workspaceId}")
    suspend fun deleteWorkspace(@Path("workspaceId") workspaceId: String)

    @POST("v3/workspaces/{workspaceId}/search")
    suspend fun searchWorkspaceMessages(
        @Path("workspaceId") workspaceId: String,
        @Body req: SearchRequest
    ): List<SearchResult>

    @GET("v3/workspaces/{workspaceId}/queue/status")
    suspend fun getQueueStatus(@Path("workspaceId") workspaceId: String): QueueStatus

    @POST("v3/workspaces/{workspaceId}/schedule_dream")
    suspend fun scheduleDream(@Path("workspaceId") workspaceId: String)

    // ===== PEERS =====

    @POST("v3/workspaces/{workspaceId}/peers")
    suspend fun getOrCreatePeer(
        @Path("workspaceId") workspaceId: String,
        @Body req: PeerUpsert
    ): Peer

    @POST("v3/workspaces/{workspaceId}/peers/list")
    suspend fun listPeers(
        @Path("workspaceId") workspaceId: String,
        @Body req: ListRequest
    ): PagedResponse<Peer>

    @PUT("v3/workspaces/{workspaceId}/peers/{peerId}")
    suspend fun updatePeer(
        @Path("workspaceId") workspaceId: String,
        @Path("peerId") peerId: String,
        @Body req: PeerUpdate
    ): Peer

    @Streaming
    @POST("v3/workspaces/{workspaceId}/peers/{peerId}/chat")
    suspend fun chatWithPeer(
        @Path("workspaceId") workspaceId: String,
        @Path("peerId") peerId: String,
        @Body req: PeerChatRequest
    ): ResponseBody

    @POST("v3/workspaces/{workspaceId}/peers/{peerId}/representation")
    suspend fun getPeerRepresentation(
        @Path("workspaceId") workspaceId: String,
        @Path("peerId") peerId: String,
        @Body req: PeerRepresentationRequest
    ): PeerRepresentation

    @GET("v3/workspaces/{workspaceId}/peers/{peerId}/card")
    suspend fun getPeerCard(
        @Path("workspaceId") workspaceId: String,
        @Path("peerId") peerId: String
    ): PeerCard

    @PUT("v3/workspaces/{workspaceId}/peers/{peerId}/card")
    suspend fun setPeerCard(
        @Path("workspaceId") workspaceId: String,
        @Path("peerId") peerId: String,
        @Body req: PeerCardUpdate
    ): PeerCard

    @GET("v3/workspaces/{workspaceId}/peers/{peerId}/context")
    suspend fun getPeerContext(
        @Path("workspaceId") workspaceId: String,
        @Path("peerId") peerId: String,
        @Query("query") query: String? = null
    ): PeerContext

    @POST("v3/workspaces/{workspaceId}/peers/{peerId}/search")
    suspend fun searchPeerMessages(
        @Path("workspaceId") workspaceId: String,
        @Path("peerId") peerId: String,
        @Body req: SearchRequest
    ): List<SearchResult>

    @POST("v3/workspaces/{workspaceId}/peers/{peerId}/sessions")
    suspend fun listPeerSessions(
        @Path("workspaceId") workspaceId: String,
        @Path("peerId") peerId: String,
        @Body req: ListRequest
    ): PagedResponse<Session>

    // ===== SESSIONS =====

    @POST("v3/workspaces/{workspaceId}/sessions")
    suspend fun getOrCreateSession(
        @Path("workspaceId") workspaceId: String,
        @Body req: SessionUpsert
    ): Session

    @POST("v3/workspaces/{workspaceId}/sessions/list")
    suspend fun listSessions(
        @Path("workspaceId") workspaceId: String,
        @Body req: ListRequest
    ): PagedResponse<Session>

    @PUT("v3/workspaces/{workspaceId}/sessions/{sessionId}")
    suspend fun updateSession(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Body req: SessionUpdate
    ): Session

    @DELETE("v3/workspaces/{workspaceId}/sessions/{sessionId}")
    suspend fun deleteSession(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String
    )

    @POST("v3/workspaces/{workspaceId}/sessions/{sessionId}/clone")
    suspend fun cloneSession(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Body req: SessionCloneRequest
    ): Session

    @GET("v3/workspaces/{workspaceId}/sessions/{sessionId}/peers")
    suspend fun getSessionPeers(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String
    ): List<Peer>

    @POST("v3/workspaces/{workspaceId}/sessions/{sessionId}/peers")
    suspend fun addPeersToSession(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Body req: SessionPeersUpdate
    ): List<Peer>

    @PUT("v3/workspaces/{workspaceId}/sessions/{sessionId}/peers")
    suspend fun setSessionPeers(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Body req: SessionPeersUpdate
    ): List<Peer>

    @HTTP(method = "DELETE", path = "v3/workspaces/{workspaceId}/sessions/{sessionId}/peers", hasBody = true)
    suspend fun removePeersFromSession(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Body req: SessionPeersUpdate
    ): List<Peer>

    @GET("v3/workspaces/{workspaceId}/sessions/{sessionId}/peers/{peerId}/config")
    suspend fun getPeerConfigInSession(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Path("peerId") peerId: String
    ): SessionPeerConfig

    @PUT("v3/workspaces/{workspaceId}/sessions/{sessionId}/peers/{peerId}/config")
    suspend fun setPeerConfigInSession(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Path("peerId") peerId: String,
        @Body req: SessionPeerConfig
    ): SessionPeerConfig

    @GET("v3/workspaces/{workspaceId}/sessions/{sessionId}/context")
    suspend fun getSessionContext(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String
    ): SessionContext

    @GET("v3/workspaces/{workspaceId}/sessions/{sessionId}/summaries")
    suspend fun getSessionSummaries(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String
    ): SessionSummaries

    @POST("v3/workspaces/{workspaceId}/sessions/{sessionId}/search")
    suspend fun searchSessionMessages(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Body req: SearchRequest
    ): List<SearchResult>

    // ===== MESSAGES =====

    @POST("v3/workspaces/{workspaceId}/sessions/{sessionId}/messages")
    suspend fun addMessages(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Body req: MessageBatchCreate
    ): List<Message>

    @Multipart
    @POST("v3/workspaces/{workspaceId}/sessions/{sessionId}/messages/upload")
    suspend fun uploadMessageFile(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Part file: MultipartBody.Part,
        @Part("peer_id") peerId: RequestBody? = null
    ): List<Message>

    @POST("v3/workspaces/{workspaceId}/sessions/{sessionId}/messages/list")
    suspend fun listMessages(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Body req: ListRequest
    ): PagedResponse<Message>

    @GET("v3/workspaces/{workspaceId}/sessions/{sessionId}/messages/{messageId}")
    suspend fun getMessage(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Path("messageId") messageId: String
    ): Message

    @PUT("v3/workspaces/{workspaceId}/sessions/{sessionId}/messages/{messageId}")
    suspend fun updateMessage(
        @Path("workspaceId") workspaceId: String,
        @Path("sessionId") sessionId: String,
        @Path("messageId") messageId: String,
        @Body req: MessageUpdate
    ): Message

    // ===== CONCLUSIONS =====

    @POST("v3/workspaces/{workspaceId}/conclusions")
    suspend fun createConclusions(
        @Path("workspaceId") workspaceId: String,
        @Body req: ConclusionBatchCreate
    ): List<Conclusion>

    @POST("v3/workspaces/{workspaceId}/conclusions/list")
    suspend fun listConclusions(
        @Path("workspaceId") workspaceId: String,
        @Body req: ListRequest
    ): PagedResponse<Conclusion>

    @POST("v3/workspaces/{workspaceId}/conclusions/query")
    suspend fun queryConclusions(
        @Path("workspaceId") workspaceId: String,
        @Body req: ConclusionQueryRequest
    ): List<Conclusion>

    @DELETE("v3/workspaces/{workspaceId}/conclusions/{conclusionId}")
    suspend fun deleteConclusion(
        @Path("workspaceId") workspaceId: String,
        @Path("conclusionId") conclusionId: String
    )

    // ===== KEYS =====

    @POST("v3/keys")
    suspend fun createKey(@Body req: KeyCreate): Key

    // ===== WEBHOOKS =====

    @POST("v3/workspaces/{workspaceId}/webhooks")
    suspend fun createWebhook(
        @Path("workspaceId") workspaceId: String,
        @Body req: WebhookCreate
    ): Webhook

    @POST("v3/workspaces/{workspaceId}/webhooks/list")
    suspend fun listWebhooks(
        @Path("workspaceId") workspaceId: String,
        @Body req: ListRequest
    ): PagedResponse<Webhook>

    @DELETE("v3/workspaces/{workspaceId}/webhooks/{webhookId}")
    suspend fun deleteWebhook(
        @Path("workspaceId") workspaceId: String,
        @Path("webhookId") webhookId: String
    )

    @POST("v3/workspaces/{workspaceId}/webhooks/{webhookId}/test")
    suspend fun testWebhook(
        @Path("workspaceId") workspaceId: String,
        @Path("webhookId") webhookId: String
    ): WebhookTestResult
}
