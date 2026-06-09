package dev.honcho.android.data

import dev.honcho.android.network.ApiClient
import dev.honcho.android.network.HonchoService
import dev.honcho.android.network.models.*
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody

class HonchoRepository(private val settingsRepository: SettingsRepository) {

    private suspend fun service(): HonchoService {
        val s = settingsRepository.settings.first()
        return ApiClient.service(s.baseUrl, s.token)
    }

    private suspend fun <T> call(block: suspend HonchoService.() -> T): Result<T> = try {
        Result.Success(service().block())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }

    // ===== WORKSPACES =====

    suspend fun getOrCreateWorkspace(workspaceId: String, name: String? = null) =
        call { getOrCreateWorkspace(WorkspaceUpsert(workspaceId = workspaceId, name = name)) }

    suspend fun listWorkspaces(page: Int = 1, pageSize: Int = 20) =
        call { listWorkspaces(ListRequest(page = page, pageSize = pageSize)) }

    suspend fun updateWorkspace(workspaceId: String, name: String? = null, metadata: Map<String, Any>? = null) =
        call { updateWorkspace(workspaceId, WorkspaceUpdate(name = name, metadata = metadata)) }

    suspend fun deleteWorkspace(workspaceId: String) =
        call { deleteWorkspace(workspaceId) }

    suspend fun searchWorkspaceMessages(workspaceId: String, query: String) =
        call { searchWorkspaceMessages(workspaceId, SearchRequest(query = query)) }

    suspend fun getQueueStatus(workspaceId: String) =
        call { getQueueStatus(workspaceId) }

    suspend fun scheduleDream(workspaceId: String) =
        call { scheduleDream(workspaceId) }

    // ===== PEERS =====

    suspend fun getOrCreatePeer(workspaceId: String, peerId: String, name: String? = null) =
        call { getOrCreatePeer(workspaceId, PeerUpsert(peerId = peerId, name = name)) }

    suspend fun listPeers(workspaceId: String, page: Int = 1, pageSize: Int = 20) =
        call { listPeers(workspaceId, ListRequest(page = page, pageSize = pageSize)) }

    suspend fun updatePeer(workspaceId: String, peerId: String, name: String? = null, metadata: Map<String, Any>? = null) =
        call { updatePeer(workspaceId, peerId, PeerUpdate(name = name, metadata = metadata)) }

    suspend fun chatWithPeer(workspaceId: String, peerId: String, query: String) =
        call { chatWithPeer(workspaceId, peerId, PeerChatRequest(query = query)).string() }

    suspend fun getPeerRepresentation(workspaceId: String, peerId: String, queries: List<String>? = null) =
        call { getPeerRepresentation(workspaceId, peerId, PeerRepresentationRequest(queries = queries)) }

    suspend fun getPeerCard(workspaceId: String, peerId: String) =
        call { getPeerCard(workspaceId, peerId) }

    suspend fun setPeerCard(workspaceId: String, peerId: String, content: String) =
        call { setPeerCard(workspaceId, peerId, PeerCardUpdate(content = content)) }

    suspend fun getPeerContext(workspaceId: String, peerId: String, query: String? = null) =
        call { getPeerContext(workspaceId, peerId, query) }

    suspend fun searchPeerMessages(workspaceId: String, peerId: String, query: String) =
        call { searchPeerMessages(workspaceId, peerId, SearchRequest(query = query)) }

    suspend fun listPeerSessions(workspaceId: String, peerId: String, page: Int = 1, pageSize: Int = 20) =
        call { listPeerSessions(workspaceId, peerId, ListRequest(page = page, pageSize = pageSize)) }

    // ===== SESSIONS =====

    suspend fun getOrCreateSession(workspaceId: String, sessionId: String? = null) =
        call { getOrCreateSession(workspaceId, SessionUpsert(sessionId = sessionId)) }

    suspend fun listSessions(workspaceId: String, page: Int = 1, pageSize: Int = 20) =
        call { listSessions(workspaceId, ListRequest(page = page, pageSize = pageSize)) }

    suspend fun updateSession(workspaceId: String, sessionId: String, metadata: Map<String, Any>? = null, isActive: Boolean? = null) =
        call { updateSession(workspaceId, sessionId, SessionUpdate(metadata = metadata, isActive = isActive)) }

    suspend fun deleteSession(workspaceId: String, sessionId: String) =
        call { deleteSession(workspaceId, sessionId) }

    suspend fun cloneSession(workspaceId: String, sessionId: String, toSessionId: String? = null, messageId: String? = null) =
        call { cloneSession(workspaceId, sessionId, SessionCloneRequest(toSessionId = toSessionId, messageId = messageId)) }

    suspend fun getSessionPeers(workspaceId: String, sessionId: String) =
        call { getSessionPeers(workspaceId, sessionId) }

    suspend fun addPeersToSession(workspaceId: String, sessionId: String, peerIds: List<String>) =
        call { addPeersToSession(workspaceId, sessionId, SessionPeersUpdate(peerIds = peerIds)) }

    suspend fun setSessionPeers(workspaceId: String, sessionId: String, peerIds: List<String>) =
        call { setSessionPeers(workspaceId, sessionId, SessionPeersUpdate(peerIds = peerIds)) }

    suspend fun removePeersFromSession(workspaceId: String, sessionId: String, peerIds: List<String>) =
        call { removePeersFromSession(workspaceId, sessionId, SessionPeersUpdate(peerIds = peerIds)) }

    suspend fun getPeerConfigInSession(workspaceId: String, sessionId: String, peerId: String) =
        call { getPeerConfigInSession(workspaceId, sessionId, peerId) }

    suspend fun setPeerConfigInSession(workspaceId: String, sessionId: String, peerId: String, config: SessionPeerConfig) =
        call { setPeerConfigInSession(workspaceId, sessionId, peerId, config) }

    suspend fun getSessionContext(workspaceId: String, sessionId: String) =
        call { getSessionContext(workspaceId, sessionId) }

    suspend fun getSessionSummaries(workspaceId: String, sessionId: String) =
        call { getSessionSummaries(workspaceId, sessionId) }

    suspend fun searchSessionMessages(workspaceId: String, sessionId: String, query: String) =
        call { searchSessionMessages(workspaceId, sessionId, SearchRequest(query = query)) }

    // ===== MESSAGES =====

    suspend fun addMessages(workspaceId: String, sessionId: String, messages: List<MessageCreate>) =
        call { addMessages(workspaceId, sessionId, MessageBatchCreate(messages = messages)) }

    suspend fun addMessage(workspaceId: String, sessionId: String, role: String, content: String, peerId: String? = null) =
        call { addMessages(workspaceId, sessionId, MessageBatchCreate(listOf(MessageCreate(role = role, content = content, peerId = peerId)))) }

    suspend fun uploadMessageFile(workspaceId: String, sessionId: String, file: MultipartBody.Part, peerId: RequestBody? = null) =
        call { uploadMessageFile(workspaceId, sessionId, file, peerId) }

    suspend fun listMessages(workspaceId: String, sessionId: String, page: Int = 1, pageSize: Int = 20) =
        call { listMessages(workspaceId, sessionId, ListRequest(page = page, pageSize = pageSize)) }

    suspend fun getMessage(workspaceId: String, sessionId: String, messageId: String) =
        call { getMessage(workspaceId, sessionId, messageId) }

    suspend fun updateMessage(workspaceId: String, sessionId: String, messageId: String, metadata: Map<String, Any>) =
        call { updateMessage(workspaceId, sessionId, messageId, MessageUpdate(metadata = metadata)) }

    // ===== CONCLUSIONS =====

    suspend fun createConclusions(workspaceId: String, conclusions: List<ConclusionCreate>) =
        call { createConclusions(workspaceId, ConclusionBatchCreate(conclusions = conclusions)) }

    suspend fun createConclusion(workspaceId: String, content: String, peerId: String? = null) =
        call { createConclusions(workspaceId, ConclusionBatchCreate(listOf(ConclusionCreate(content = content, peerId = peerId)))) }

    suspend fun listConclusions(workspaceId: String, page: Int = 1, pageSize: Int = 20) =
        call { listConclusions(workspaceId, ListRequest(page = page, pageSize = pageSize)) }

    suspend fun queryConclusions(workspaceId: String, query: String, peerId: String? = null, topK: Int = 10) =
        call { queryConclusions(workspaceId, ConclusionQueryRequest(query = query, peerId = peerId, topK = topK)) }

    suspend fun deleteConclusion(workspaceId: String, conclusionId: String) =
        call { deleteConclusion(workspaceId, conclusionId) }

    // ===== KEYS =====

    suspend fun createKey(workspaceId: String? = null, peerId: String? = null, sessionId: String? = null) =
        call { createKey(KeyCreate(workspaceId = workspaceId, peerId = peerId, sessionId = sessionId)) }

    // ===== WEBHOOKS =====

    suspend fun createWebhook(workspaceId: String, url: String, events: List<String>? = null) =
        call { createWebhook(workspaceId, WebhookCreate(url = url, events = events)) }

    suspend fun listWebhooks(workspaceId: String, page: Int = 1, pageSize: Int = 20) =
        call { listWebhooks(workspaceId, ListRequest(page = page, pageSize = pageSize)) }

    suspend fun deleteWebhook(workspaceId: String, webhookId: String) =
        call { deleteWebhook(workspaceId, webhookId) }

    suspend fun testWebhook(workspaceId: String, webhookId: String) =
        call { testWebhook(workspaceId, webhookId) }
}
