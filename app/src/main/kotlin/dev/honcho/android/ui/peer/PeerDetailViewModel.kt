package dev.honcho.android.ui.peer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.honcho.android.AppModule
import dev.honcho.android.network.models.Peer
import dev.honcho.android.network.models.PeerCard
import dev.honcho.android.network.models.PeerContext
import dev.honcho.android.network.models.PeerRepresentation
import dev.honcho.android.network.models.SearchResult
import dev.honcho.android.network.models.Session
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PeerDetailUiState(
    val peer: Peer? = null,
    val card: PeerCard? = null,
    val context: PeerContext? = null,
    val representation: PeerRepresentation? = null,
    val chatResponse: String? = null,
    val sessions: List<Session> = emptyList(),
    val searchResults: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingCard: Boolean = false,
    val isLoadingContext: Boolean = false,
    val isLoadingRepresentation: Boolean = false,
    val isChatting: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null
)

class PeerDetailViewModel(val workspaceId: String, val peerId: String) : ViewModel() {
    private val repository = AppModule.honchoRepository

    private val _uiState = MutableStateFlow(PeerDetailUiState())
    val uiState: StateFlow<PeerDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        loadPeer()
        loadCard()
    }

    fun loadPeer() {
        viewModelScope.launch {
            when (val r = repository.getOrCreatePeer(workspaceId, peerId)) {
                is Result.Success -> _uiState.update { it.copy(peer = r.data) }
                is Result.Error -> _uiState.update { it.copy(error = r.message) }
                else -> {}
            }
        }
    }

    fun loadCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCard = true) }
            when (val r = repository.getPeerCard(workspaceId, peerId)) {
                is Result.Success -> _uiState.update { it.copy(isLoadingCard = false, card = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoadingCard = false) }
                else -> {}
            }
        }
    }

    fun setCard(content: String) {
        viewModelScope.launch {
            when (val r = repository.setPeerCard(workspaceId, peerId, content)) {
                is Result.Success -> { _uiState.update { it.copy(card = r.data) }; _events.emit("Card updated") }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun loadContext(query: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingContext = true) }
            when (val r = repository.getPeerContext(workspaceId, peerId, query)) {
                is Result.Success -> _uiState.update { it.copy(isLoadingContext = false, context = r.data) }
                is Result.Error -> { _uiState.update { it.copy(isLoadingContext = false) }; _events.emit("Error: ${r.message}") }
                else -> {}
            }
        }
    }

    fun loadRepresentation(queries: List<String>? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRepresentation = true) }
            when (val r = repository.getPeerRepresentation(workspaceId, peerId, queries)) {
                is Result.Success -> _uiState.update { it.copy(isLoadingRepresentation = false, representation = r.data) }
                is Result.Error -> { _uiState.update { it.copy(isLoadingRepresentation = false) }; _events.emit("Error: ${r.message}") }
                else -> {}
            }
        }
    }

    fun chat(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isChatting = true, chatResponse = null) }
            when (val r = repository.chatWithPeer(workspaceId, peerId, query)) {
                is Result.Success -> _uiState.update { it.copy(isChatting = false, chatResponse = r.data) }
                is Result.Error -> { _uiState.update { it.copy(isChatting = false) }; _events.emit("Chat error: ${r.message}") }
                else -> {}
            }
        }
    }

    fun searchMessages(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            when (val r = repository.searchPeerMessages(workspaceId, peerId, query)) {
                is Result.Success -> _uiState.update { it.copy(isSearching = false, searchResults = r.data) }
                is Result.Error -> { _uiState.update { it.copy(isSearching = false) }; _events.emit("Error: ${r.message}") }
                else -> {}
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            when (val r = repository.listPeerSessions(workspaceId, peerId)) {
                is Result.Success -> _uiState.update { it.copy(sessions = r.data.items) }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun updatePeer(name: String?) {
        viewModelScope.launch {
            when (val r = repository.updatePeer(workspaceId, peerId, name = name?.takeIf { it.isNotBlank() })) {
                is Result.Success -> { _uiState.update { it.copy(peer = r.data) }; _events.emit("Peer updated") }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }
}
