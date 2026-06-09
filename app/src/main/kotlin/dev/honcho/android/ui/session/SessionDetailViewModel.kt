package dev.honcho.android.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.honcho.android.AppModule
import dev.honcho.android.network.models.Message
import dev.honcho.android.network.models.Peer
import dev.honcho.android.network.models.SearchResult
import dev.honcho.android.network.models.Session
import dev.honcho.android.network.models.SessionContext
import dev.honcho.android.network.models.SessionPeerConfig
import dev.honcho.android.network.models.SessionSummaries
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionDetailUiState(
    val session: Session? = null,
    val messages: List<Message> = emptyList(),
    val hasMoreMessages: Boolean = false,
    val messagesPage: Int = 1,
    val sessionPeers: List<Peer> = emptyList(),
    val sessionContext: SessionContext? = null,
    val sessionSummaries: SessionSummaries? = null,
    val searchResults: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null
)

class SessionDetailViewModel(val workspaceId: String, val sessionId: String) : ViewModel() {
    private val repository = AppModule.honchoRepository

    private val _uiState = MutableStateFlow(SessionDetailUiState())
    val uiState: StateFlow<SessionDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        loadMessages()
        loadSessionPeers()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, messagesPage = 1) }
            when (val r = repository.listMessages(workspaceId, sessionId, page = 1)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, messages = r.data.items, hasMoreMessages = r.data.hasMore, messagesPage = 1)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                else -> {}
            }
        }
    }

    fun loadMoreMessages() {
        val state = _uiState.value
        if (!state.hasMoreMessages || state.isLoading) return
        val nextPage = state.messagesPage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = repository.listMessages(workspaceId, sessionId, page = nextPage)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, messages = r.data.items + it.messages, hasMoreMessages = r.data.hasMore, messagesPage = nextPage)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false) }
                else -> {}
            }
        }
    }

    fun sendMessage(role: String, content: String, peerId: String? = null) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            when (val r = repository.addMessage(workspaceId, sessionId, role, content, peerId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSending = false, messages = it.messages + r.data) }
                }
                is Result.Error -> { _uiState.update { it.copy(isSending = false) }; _events.emit("Error: ${r.message}") }
                else -> {}
            }
        }
    }

    fun loadSessionPeers() {
        viewModelScope.launch {
            when (val r = repository.getSessionPeers(workspaceId, sessionId)) {
                is Result.Success -> _uiState.update { it.copy(sessionPeers = r.data) }
                is Result.Error -> _events.emit("Error loading peers: ${r.message}")
                else -> {}
            }
        }
    }

    fun addPeer(peerId: String) {
        viewModelScope.launch {
            when (val r = repository.addPeersToSession(workspaceId, sessionId, listOf(peerId))) {
                is Result.Success -> { _uiState.update { it.copy(sessionPeers = r.data) }; _events.emit("Peer added") }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun removePeer(peerId: String) {
        viewModelScope.launch {
            when (val r = repository.removePeersFromSession(workspaceId, sessionId, listOf(peerId))) {
                is Result.Success -> { _uiState.update { it.copy(sessionPeers = r.data) }; _events.emit("Peer removed") }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun setPeerConfig(peerId: String, observe: Boolean?, respond: Boolean?) {
        viewModelScope.launch {
            when (val r = repository.setPeerConfigInSession(workspaceId, sessionId, peerId, SessionPeerConfig(observe = observe, respond = respond))) {
                is Result.Success -> _events.emit("Peer config updated")
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun loadContext() {
        viewModelScope.launch {
            when (val r = repository.getSessionContext(workspaceId, sessionId)) {
                is Result.Success -> _uiState.update { it.copy(sessionContext = r.data) }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun loadSummaries() {
        viewModelScope.launch {
            when (val r = repository.getSessionSummaries(workspaceId, sessionId)) {
                is Result.Success -> _uiState.update { it.copy(sessionSummaries = r.data) }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun cloneSession(toSessionId: String?) {
        viewModelScope.launch {
            when (val r = repository.cloneSession(workspaceId, sessionId, toSessionId?.takeIf { it.isNotBlank() })) {
                is Result.Success -> _events.emit("Cloned to session '${r.data.sessionId}'")
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun searchMessages(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            when (val r = repository.searchSessionMessages(workspaceId, sessionId, query)) {
                is Result.Success -> _uiState.update { it.copy(isSearching = false, searchResults = r.data) }
                is Result.Error -> { _uiState.update { it.copy(isSearching = false) }; _events.emit("Error: ${r.message}") }
                else -> {}
            }
        }
    }

    fun updateSession(isActive: Boolean) {
        viewModelScope.launch {
            when (val r = repository.updateSession(workspaceId, sessionId, isActive = isActive)) {
                is Result.Success -> { _uiState.update { it.copy(session = r.data) }; _events.emit("Session updated") }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }
}
