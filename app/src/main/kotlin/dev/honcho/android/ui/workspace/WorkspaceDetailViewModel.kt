package dev.honcho.android.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.honcho.android.AppModule
import dev.honcho.android.network.models.Peer
import dev.honcho.android.network.models.QueueStatus
import dev.honcho.android.network.models.SearchResult
import dev.honcho.android.network.models.Session
import dev.honcho.android.network.models.Workspace
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkspaceDetailUiState(
    val workspace: Workspace? = null,
    val isLoadingPeers: Boolean = false,
    val peers: List<Peer> = emptyList(),
    val hasMorePeers: Boolean = false,
    val peersPage: Int = 1,
    val isLoadingSessions: Boolean = false,
    val sessions: List<Session> = emptyList(),
    val hasMoreSessions: Boolean = false,
    val sessionsPage: Int = 1,
    val queueStatus: QueueStatus? = null,
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)

class WorkspaceDetailViewModel(val workspaceId: String) : ViewModel() {
    private val repository = AppModule.honchoRepository

    private val _uiState = MutableStateFlow(WorkspaceDetailUiState())
    val uiState: StateFlow<WorkspaceDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        loadPeers()
        loadSessions()
    }

    fun loadPeers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPeers = true, peersPage = 1) }
            when (val r = repository.listPeers(workspaceId, page = 1)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingPeers = false, peers = r.data.items, hasMorePeers = r.data.hasMore, peersPage = 1)
                }
                is Result.Error -> _uiState.update { it.copy(isLoadingPeers = false, error = r.message) }
                else -> {}
            }
        }
    }

    fun loadMorePeers() {
        val state = _uiState.value
        if (!state.hasMorePeers || state.isLoadingPeers) return
        val nextPage = state.peersPage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPeers = true) }
            when (val r = repository.listPeers(workspaceId, page = nextPage)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingPeers = false, peers = it.peers + r.data.items, hasMorePeers = r.data.hasMore, peersPage = nextPage)
                }
                is Result.Error -> _uiState.update { it.copy(isLoadingPeers = false, error = r.message) }
                else -> {}
            }
        }
    }

    fun createPeer(peerId: String, name: String?) {
        if (peerId.isBlank()) return
        viewModelScope.launch {
            when (val r = repository.getOrCreatePeer(workspaceId, peerId, name?.takeIf { it.isNotBlank() })) {
                is Result.Success -> { _events.emit("Peer '${r.data.peerId}' ready"); loadPeers() }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSessions = true, sessionsPage = 1) }
            when (val r = repository.listSessions(workspaceId, page = 1)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingSessions = false, sessions = r.data.items, hasMoreSessions = r.data.hasMore, sessionsPage = 1)
                }
                is Result.Error -> _uiState.update { it.copy(isLoadingSessions = false, error = r.message) }
                else -> {}
            }
        }
    }

    fun loadMoreSessions() {
        val state = _uiState.value
        if (!state.hasMoreSessions || state.isLoadingSessions) return
        val nextPage = state.sessionsPage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSessions = true) }
            when (val r = repository.listSessions(workspaceId, page = nextPage)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingSessions = false, sessions = it.sessions + r.data.items, hasMoreSessions = r.data.hasMore, sessionsPage = nextPage)
                }
                is Result.Error -> _uiState.update { it.copy(isLoadingSessions = false, error = r.message) }
                else -> {}
            }
        }
    }

    fun createSession(sessionId: String?) {
        viewModelScope.launch {
            when (val r = repository.getOrCreateSession(workspaceId, sessionId?.takeIf { it.isNotBlank() })) {
                is Result.Success -> { _events.emit("Session '${r.data.sessionId}' ready"); loadSessions() }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            when (val r = repository.deleteSession(workspaceId, sessionId)) {
                is Result.Success -> {
                    _events.emit("Session deleted")
                    _uiState.update { it.copy(sessions = it.sessions.filter { s -> s.sessionId != sessionId }) }
                }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun refreshQueueStatus() {
        viewModelScope.launch {
            when (val r = repository.getQueueStatus(workspaceId)) {
                is Result.Success -> _uiState.update { it.copy(queueStatus = r.data) }
                is Result.Error -> _events.emit("Queue status error: ${r.message}")
                else -> {}
            }
        }
    }

    fun scheduleDream() {
        viewModelScope.launch {
            when (val r = repository.scheduleDream(workspaceId)) {
                is Result.Success -> _events.emit("Dream task scheduled")
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            when (val r = repository.searchWorkspaceMessages(workspaceId, query)) {
                is Result.Success -> _uiState.update { it.copy(isSearching = false, searchResults = r.data) }
                is Result.Error -> { _uiState.update { it.copy(isSearching = false) }; _events.emit("Search error: ${r.message}") }
                else -> {}
            }
        }
    }
}
