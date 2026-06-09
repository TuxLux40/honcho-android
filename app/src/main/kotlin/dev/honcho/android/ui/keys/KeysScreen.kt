package dev.honcho.android.ui.keys

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeysScreen(
    vm: KeysViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var workspaceId by remember { mutableStateOf("") }
    var peerId by remember { mutableStateOf("") }
    var sessionId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.events.collect { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("API Keys") },
                navigationIcon = { IconButton(onClick = onNavigateUp) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Create a scoped API key", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Leave all fields blank for a workspace-root key. Scoped keys limit access to a specific workspace, peer, or session.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(value = workspaceId, onValueChange = { workspaceId = it }, label = { Text("Workspace ID (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = peerId, onValueChange = { peerId = it }, label = { Text("Peer ID (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = sessionId, onValueChange = { sessionId = it }, label = { Text("Session ID (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = { vm.createKey(workspaceId, peerId, sessionId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isCreating
            ) {
                if (state.isCreating) { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text("Create key")
            }

            state.lastCreatedKey?.let { key ->
                HorizontalDivider()
                Text("New key (copy it now — it won't be shown again):", style = MaterialTheme.typography.labelMedium)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SelectionContainer {
                            Text(key.key, style = MaterialTheme.typography.bodyMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                        key.scope?.let { Text("Scope: $it", style = MaterialTheme.typography.labelSmall) }
                    }
                }
                TextButton(onClick = vm::clearLastKey, modifier = Modifier.align(Alignment.End)) { Text("Dismiss") }
            }
        }
    }
}
