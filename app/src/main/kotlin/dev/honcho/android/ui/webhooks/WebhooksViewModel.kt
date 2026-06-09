package dev.honcho.android.ui.webhooks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.honcho.android.AppModule
import dev.honcho.android.network.models.Webhook
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WebhooksUiState(
    val isLoading: Boolean = false,
    val webhooks: List<Webhook> = emptyList(),
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
    val error: String? = null
)

class WebhooksViewModel(val workspaceId: String) : ViewModel() {
    private val repository = AppModule.honchoRepository

    private val _uiState = MutableStateFlow(WebhooksUiState())
    val uiState: StateFlow<WebhooksUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentPage = 1) }
            when (val r = repository.listWebhooks(workspaceId, page = 1)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, webhooks = r.data.items, hasMore = r.data.hasMore, currentPage = 1)
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
            when (val r = repository.listWebhooks(workspaceId, page = nextPage)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, webhooks = it.webhooks + r.data.items, hasMore = r.data.hasMore, currentPage = nextPage)
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false) }
                else -> {}
            }
        }
    }

    fun create(url: String, events: List<String>?) {
        if (url.isBlank()) return
        viewModelScope.launch {
            when (val r = repository.createWebhook(workspaceId, url, events?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() })) {
                is Result.Success -> { _events.emit("Webhook created"); load() }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun delete(webhookId: String) {
        viewModelScope.launch {
            when (val r = repository.deleteWebhook(workspaceId, webhookId)) {
                is Result.Success -> {
                    _events.emit("Webhook deleted")
                    _uiState.update { it.copy(webhooks = it.webhooks.filter { w -> w.webhookId != webhookId }) }
                }
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }

    fun test(webhookId: String) {
        viewModelScope.launch {
            when (val r = repository.testWebhook(workspaceId, webhookId)) {
                is Result.Success -> _events.emit("Test result: ${if (r.data.success == true) "success" else r.data.message ?: "sent"}")
                is Result.Error -> _events.emit("Error: ${r.message}")
                else -> {}
            }
        }
    }
}
