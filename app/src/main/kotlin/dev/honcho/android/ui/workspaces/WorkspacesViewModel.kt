package dev.honcho.android.ui.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.honcho.android.AppModule
import dev.honcho.android.network.models.Workspace
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkspacesUiState(
    val isLoading: Boolean = false,
    val workspaces: List<Workspace> = emptyList(),
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
    val error: String? = null
)

class WorkspacesViewModel : ViewModel() {
    private val repository = AppModule.honchoRepository

    private val _uiState = MutableStateFlow(WorkspacesUiState())
    val uiState: StateFlow<WorkspacesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentPage = 1) }
            when (val r = repository.listWorkspaces(page = 1)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, workspaces = r.data.items, hasMore = r.data.hasMore, currentPage = 1)
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
            when (val r = repository.listWorkspaces(page = nextPage)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, workspaces = it.workspaces + r.data.items, hasMore = r.data.hasMore, currentPage = nextPage)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                else -> {}
            }
        }
    }

    fun createWorkspace(workspaceId: String, name: String?) {
        if (workspaceId.isBlank()) return
        viewModelScope.launch {
            when (val r = repository.getOrCreateWorkspace(workspaceId, name?.takeIf { it.isNotBlank() })) {
                is Result.Success -> { _events.emit("Workspace '${r.data.workspaceId}' ready"); load() }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun deleteWorkspace(workspaceId: String) {
        viewModelScope.launch {
            when (val r = repository.deleteWorkspace(workspaceId)) {
                is Result.Success -> {
                    _events.emit("Workspace deleted")
                    _uiState.update { it.copy(workspaces = it.workspaces.filter { w -> w.workspaceId != workspaceId }) }
                }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }
}
