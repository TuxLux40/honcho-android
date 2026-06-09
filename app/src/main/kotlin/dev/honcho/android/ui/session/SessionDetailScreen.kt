package dev.honcho.android.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.honcho.android.network.models.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    workspaceId: String,
    sessionId: String,
    onNavigateUp: () -> Unit
) {
    val vm: SessionDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SessionDetailViewModel(workspaceId, sessionId) as T
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Messages", "Peers", "Context", "Search")

    LaunchedEffect(Unit) { vm.events.collect { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(sessionId, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onNavigateUp) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }
            when (selectedTab) {
                0 -> MessagesTab(state, vm)
                1 -> PeersTab(state, vm)
                2 -> ContextTab(state, vm)
                3 -> SearchTab(state, vm)
            }
        }
    }
}

@Composable
private fun MessagesTab(state: SessionDetailUiState, vm: SessionDetailViewModel) {
    var messageInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("user") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Column(Modifier.fillMaxSize()) {
        // Load more at top
        if (state.hasMoreMessages) {
            Box(Modifier.fillMaxWidth().padding(4.dp), contentAlignment = Alignment.Center) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                else TextButton(onClick = vm::loadMoreMessages) { Text("Load earlier messages") }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.messages, key = { it.messageId }) { msg ->
                MessageBubble(msg)
            }
        }

        // Input bar
        Surface(shadowElevation = 4.dp) {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Role toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("user", "assistant").forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message…") },
                        maxLines = 4
                    )
                    IconButton(
                        onClick = { vm.sendMessage(selectedRole, messageInput); messageInput = "" },
                        enabled = messageInput.isNotBlank() && !state.isSending
                    ) {
                        if (state.isSending) CircularProgressIndicator(Modifier.size(20.dp))
                        else Icon(Icons.AutoMirrored.Filled.Send, "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Text(
                text = message.role,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PeersTab(state: SessionDetailUiState, vm: SessionDetailViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showCloneDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAddDialog = true }) { Text("Add peer") }
            OutlinedButton(onClick = { showCloneDialog = true }) { Text("Clone session") }
        }

        Text("Session peers:", style = MaterialTheme.typography.titleSmall)
        if (state.sessionPeers.isEmpty()) {
            Text("No peers in this session", style = MaterialTheme.typography.bodySmall)
        }
        state.sessionPeers.forEach { peer ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(peer.peerId, style = MaterialTheme.typography.bodyMedium)
                        peer.name?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                    TextButton(onClick = { vm.removePeer(peer.peerId) }) { Text("Remove") }
                }
            }
        }
    }

    if (showAddDialog) {
        var peerId by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add peer to session") },
            text = { OutlinedTextField(value = peerId, onValueChange = { peerId = it }, label = { Text("Peer ID") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { vm.addPeer(peerId); showAddDialog = false }, enabled = peerId.isNotBlank()) { Text("Add") } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCloneDialog) {
        var toId by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCloneDialog = false },
            title = { Text("Clone session") },
            text = { OutlinedTextField(value = toId, onValueChange = { toId = it }, label = { Text("Target session ID (leave blank for auto)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { vm.cloneSession(toId); showCloneDialog = false }) { Text("Clone") } },
            dismissButton = { TextButton(onClick = { showCloneDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ContextTab(state: SessionDetailUiState, vm: SessionDetailViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::loadContext) { Text("Load context") }
            OutlinedButton(onClick = vm::loadSummaries) { Text("Load summaries") }
        }
        state.sessionContext?.let { ctx ->
            Text("Context:", style = MaterialTheme.typography.titleSmall)
            ctx.context?.let { Text(it) }
            ctx.summaries?.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
        state.sessionSummaries?.let { s ->
            Text("Summaries:", style = MaterialTheme.typography.titleSmall)
            s.summaries?.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun SearchTab(state: SessionDetailUiState, vm: SessionDetailViewModel) {
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search messages") }, modifier = Modifier.weight(1f), singleLine = true)
            Button(onClick = { vm.searchMessages(query) }, enabled = query.isNotBlank() && !state.isSearching) {
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
