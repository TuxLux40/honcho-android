package dev.honcho.android.ui.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.honcho.android.AppModule
import dev.honcho.android.network.models.Key
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KeysUiState(
    val isCreating: Boolean = false,
    val lastCreatedKey: Key? = null,
    val error: String? = null
)

class KeysViewModel : ViewModel() {
    private val repository = AppModule.honchoRepository

    private val _uiState = MutableStateFlow(KeysUiState())
    val uiState: StateFlow<KeysUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun createKey(workspaceId: String?, peerId: String?, sessionId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null, lastCreatedKey = null) }
            when (val r = repository.createKey(
                workspaceId = workspaceId?.takeIf { it.isNotBlank() },
                peerId = peerId?.takeIf { it.isNotBlank() },
                sessionId = sessionId?.takeIf { it.isNotBlank() }
            )) {
                is Result.Success -> _uiState.update { it.copy(isCreating = false, lastCreatedKey = r.data) }
                is Result.Error -> _uiState.update { it.copy(isCreating = false, error = r.message) }
                else -> {}
            }
        }
    }

    fun clearLastKey() = _uiState.update { it.copy(lastCreatedKey = null) }
}
