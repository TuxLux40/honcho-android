package dev.honcho.android.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.honcho.android.AppModule
import dev.honcho.android.data.SettingsRepository
import dev.honcho.android.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetupUiState(
    val baseUrl: String = SettingsRepository.DEFAULT_BASE_URL,
    val token: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class SetupViewModel : ViewModel() {
    private val settingsRepository = AppModule.settingsRepository
    private val repository = AppModule.honchoRepository

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            _uiState.update { it.copy(baseUrl = settings.baseUrl, token = settings.token) }
        }
    }

    fun setBaseUrl(url: String) = _uiState.update { it.copy(baseUrl = url) }
    fun setToken(token: String) = _uiState.update { it.copy(token = token) }

    fun saveAndValidate(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.token.isBlank()) {
            _uiState.update { it.copy(error = "Bearer token is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            settingsRepository.saveSettings(state.baseUrl, state.token)
            when (val result = repository.listWorkspaces()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = "Validation failed: ${result.message}") }
                }
                else -> {}
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            settingsRepository.clearSettings()
            dev.honcho.android.network.ApiClient.invalidate()
        }
    }
}
