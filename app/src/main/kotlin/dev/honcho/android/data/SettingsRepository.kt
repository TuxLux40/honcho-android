package dev.honcho.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.honcho.android.util.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "honcho_prefs")

data class Settings(
    val baseUrl: String,
    val token: String
) {
    val isConfigured: Boolean get() = baseUrl.isNotBlank() && token.isNotBlank()
}

class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        const val DEFAULT_BASE_URL = "https://api.honcho.dev"
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            baseUrl = prefs[KEY_BASE_URL] ?: DEFAULT_BASE_URL,
            token = SecureStorage.getToken(context)
        )
    }

    suspend fun saveSettings(baseUrl: String, token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = baseUrl.ifBlank { DEFAULT_BASE_URL }
        }
        SecureStorage.saveToken(context, token)
    }

    suspend fun clearSettings() {
        context.dataStore.edit { it.clear() }
        SecureStorage.clearToken(context)
    }
}
