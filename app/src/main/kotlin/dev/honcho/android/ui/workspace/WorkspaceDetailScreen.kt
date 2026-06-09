package dev.honcho.android.ui.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceDetailScreen(
    workspaceId: String,
    onPeerClick: (String) -> Unit,
    onSessionClick: (String) -> Unit,
    onConclusionsClick: () -> Unit,
    onWebhooksClick: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val vm: WorkspaceDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WorkspaceDetailViewModel(workspaceId) as T
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Peers", "Sessions", "Search")

    LaunchedEffect(Unit) {
        vm.events.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(workspaceId, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    TextButton(onClick = onConclusionsClick) { Text("Conclusions") }
                    TextButton(onClick = onWebhooksClick) { Text("Webhooks") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Queue status bar
            state.queueStatus?.let { qs ->
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Queue: ${qs.status ?: "unknown"} | pending: ${qs.pending ?: 0}", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = vm::scheduleDream, contentPadding = PaddingValues(0.dp)) { Text("Dream") }
                    }
                }
            }
            Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = vm::refreshQueueStatus) { Text("Queue status") }
            }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> PeersTab(state, vm, onPeerClick)
                1 -> SessionsTab(state, vm, onSessionClick)
                2 -> SearchTab(state, vm)
            }
        }
    }
}

@Composable
private fun PeersTab(state: WorkspaceDetailUiState, vm: WorkspaceDetailViewModel, onPeerClick: (String) -> Unit) {
    var showCreate by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (state.isLoadingPeers && state.peers.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.peers, key = { it.peerId }) { peer ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onPeerClick(peer.peerId) }) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(peer.peerId, style = MaterialTheme.typography.titleSmall)
                                peer.name?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
                if (state.hasMorePeers) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (state.isLoadingPeers) CircularProgressIndicator(Modifier.size(24.dp))
                            else TextButton(onClick = vm::loadMorePeers) { Text("Load more") }
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showCreate = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Icon(Icons.Default.Add, "Create peer")
        }
    }

    if (showCreate) {
        var id by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Create peer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Peer ID *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.createPeer(id, name); showCreate = false }, enabled = id.isNotBlank()) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SessionsTab(state: WorkspaceDetailUiState, vm: WorkspaceDetailViewModel, onSessionClick: (String) -> Unit) {
    var showCreate by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (state.isLoadingSessions && state.sessions.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.sessions, key = { it.sessionId }) { session ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onSessionClick(session.sessionId) }) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(session.sessionId, style = MaterialTheme.typography.titleSmall)
                                Text(if (session.isActive) "Active" else "Inactive", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { vm.deleteSession(session.sessionId) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                if (state.hasMoreSessions) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (state.isLoadingSessions) CircularProgressIndicator(Modifier.size(24.dp))
                            else TextButton(onClick = vm::loadMoreSessions) { Text("Load more") }
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showCreate = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Icon(Icons.Default.Add, "Create session")
        }
    }

    if (showCreate) {
        var id by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Create session") },
            text = {
                OutlinedTextField(value = id, onValueChange = { id = it }, label = { Text("Session ID (leave blank for auto)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = { vm.createSession(id); showCreate = false }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SearchTab(state: WorkspaceDetailUiState, vm: WorkspaceDetailViewModel) {
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search messages") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(onClick = { vm.search(query) }, enabled = query.isNotBlank() && !state.isSearching) {
                if (state.isSearching) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) else Text("Search")
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.searchResults) { result ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(result.content ?: "", style = MaterialTheme.typography.bodyMedium)
                        result.score?.let { Text("Score: %.3f".format(it), style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}
