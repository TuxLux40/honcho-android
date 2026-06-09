package dev.honcho.android.ui.peer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun PeerDetailScreen(
    workspaceId: String,
    peerId: String,
    onNavigateUp: () -> Unit
) {
    val vm: PeerDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PeerDetailViewModel(workspaceId, peerId) as T
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Card", "Context", "Representation", "Chat", "Search")

    LaunchedEffect(Unit) { vm.events.collect { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(peerId, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onNavigateUp) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title) })
                }
            }
            when (selectedTab) {
                0 -> InfoTab(state, vm)
                1 -> CardTab(state, vm)
                2 -> ContextTab(state, vm)
                3 -> RepresentationTab(state, vm)
                4 -> ChatTab(state, vm)
                5 -> SearchTab(state, vm)
            }
        }
    }
}

@Composable
private fun InfoTab(state: PeerDetailUiState, vm: PeerDetailViewModel) {
    var editName by remember(state.peer) { mutableStateOf(state.peer?.name ?: "") }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.peer?.let { peer ->
            InfoRow("Peer ID", peer.peerId)
            InfoRow("Workspace ID", peer.workspaceId)
            peer.name?.let { InfoRow("Name", it) }
        }
        HorizontalDivider()
        Text("Update peer", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = { vm.updatePeer(editName) }) { Text("Save") }
    }
}

@Composable
private fun CardTab(state: PeerDetailUiState, vm: PeerDetailViewModel) {
    var editContent by remember(state.card) { mutableStateOf(state.card?.content ?: "") }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.isLoadingCard) {
            CircularProgressIndicator()
        } else {
            Text("Peer Card", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = editContent,
                onValueChange = { editContent = it },
                label = { Text("Card content") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                maxLines = 10
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.setCard(editContent) }, enabled = editContent.isNotBlank()) { Text("Save card") }
                OutlinedButton(onClick = vm::loadCard) { Text("Reload") }
            }
        }
    }
}

@Composable
private fun ContextTab(state: PeerDetailUiState, vm: PeerDetailViewModel) {
    var query by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Optional query") }, modifier = Modifier.weight(1f), singleLine = true)
            Button(onClick = { vm.loadContext(query.takeIf { it.isNotBlank() }) }) { Text("Load") }
        }
        if (state.isLoadingContext) CircularProgressIndicator()
        state.context?.let { ctx ->
            ctx.context?.let { Text(it) }
            ctx.facts?.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun RepresentationTab(state: PeerDetailUiState, vm: PeerDetailViewModel) {
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { vm.loadRepresentation() }) { Text("Load representation") }
        if (state.isLoadingRepresentation) CircularProgressIndicator()
        state.representation?.let { rep ->
            rep.content?.let { Text(it) }
            rep.facts?.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun ChatTab(state: PeerDetailUiState, vm: PeerDetailViewModel) {
    var query by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Ask the peer…") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        Button(onClick = { vm.chat(query) }, enabled = query.isNotBlank() && !state.isChatting, modifier = Modifier.fillMaxWidth()) {
            if (state.isChatting) { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
            Text("Send")
        }
        state.chatResponse?.let {
            HorizontalDivider()
            Text("Response:", style = MaterialTheme.typography.labelMedium)
            Text(it)
        }
    }
}

@Composable
private fun SearchTab(state: PeerDetailUiState, vm: PeerDetailViewModel) {
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

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
