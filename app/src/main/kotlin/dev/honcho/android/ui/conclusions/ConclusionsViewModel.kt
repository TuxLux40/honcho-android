package dev.honcho.android.ui.conclusions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.honcho.android.AppModule
import dev.honcho.android.network.models.Conclusion
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConclusionsUiState(
    val isLoading: Boolean = false,
    val conclusions: List<Conclusion> = emptyList(),
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
    val queryResults: List<Conclusion> = emptyList(),
    val isQuerying: Boolean = false,
    val error: String? = null
)

class ConclusionsViewModel(val workspaceId: String) : ViewModel() {
    private val repository = AppModule.honchoRepository

    private val _uiState = MutableStateFlow(ConclusionsUiState())
    val uiState: StateFlow<ConclusionsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentPage = 1) }
            when (val r = repository.listConclusions(workspaceId, page = 1)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, conclusions = r.data.items, hasMore = r.data.hasMore, currentPage = 1)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                else -> {}
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoading) return
        val nextPage = state.currentPage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = repository.listConclusions(workspaceId, page = nextPage)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, conclusions = it.conclusions + r.data.items, hasMore = r.data.hasMore, currentPage = nextPage)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false) }
                else -> {}
            }
        }
    }

    fun create(content: String, peerId: String?) {
        if (content.isBlank()) return
        viewModelScope.launch {
            when (val r = repository.createConclusion(workspaceId, content, peerId?.takeIf { it.isNotBlank() })) {
                is Result.Success -> { _events.emit("Conclusion created"); load() }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun delete(conclusionId: String) {
        viewModelScope.launch {
            when (val r = repository.deleteConclusion(workspaceId, conclusionId)) {
                is Result.Success -> {
                    _events.emit("Conclusion deleted")
                    _uiState.update { it.copy(conclusions = it.conclusions.filter { c -> c.conclusionId != conclusionId }) }
                }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun query(query: String, peerId: String?, topK: Int = 10) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isQuerying = true) }
            when (val r = repository.queryConclusions(workspaceId, query, peerId?.takeIf { it.isNotBlank() }, topK)) {
                is Result.Success -> _uiState.update { it.copy(isQuerying = false, queryResults = r.data) }
                is Result.Error -> { _uiState.update { it.copy(isQuerying = false) }; _events.emit("Error: ${r.message}") }
                else -> {}
            }
        }
    }
}
