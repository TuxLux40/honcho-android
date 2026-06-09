package dev.honcho.android.ui.conclusions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
fun ConclusionsScreen(
    workspaceId: String,
    onNavigateUp: () -> Unit
) {
    val vm: ConclusionsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ConclusionsViewModel(workspaceId) as T
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.events.collect { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Conclusions") },
                navigationIcon = { IconButton(onClick = onNavigateUp) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, "Create") }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("List") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Query") })
            }

            when (selectedTab) {
                0 -> ListTab(state, vm)
                1 -> QueryTab(state, vm)
            }
        }
    }

    if (showCreateDialog) {
        var content by remember { mutableStateOf("") }
        var peerId by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New conclusion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content *") }, modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), maxLines = 5)
                    OutlinedTextField(value = peerId, onValueChange = { peerId = it }, label = { Text("Peer ID (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.create(content, peerId); showCreateDialog = false }, enabled = content.isNotBlank()) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ListTab(state: ConclusionsUiState, vm: ConclusionsViewModel) {
    Box(Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.conclusions.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.error != null && state.conclusions.isEmpty() -> {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    Button(onClick = vm::load) { Text("Retry") }
                }
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.conclusions, key = { it.conclusionId }) { c ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(c.content, style = MaterialTheme.typography.bodyMedium)
                                    c.peerId?.let { Text("Peer: $it", style = MaterialTheme.typography.labelSmall) }
                                }
                                IconButton(onClick = { vm.delete(c.conclusionId) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    if (state.hasMore) {
                        item {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                if (state.isLoading) CircularProgressIndicator(Modifier.size(24.dp))
                                else TextButton(onClick = vm::loadMore) { Text("Load more") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueryTab(state: ConclusionsUiState, vm: ConclusionsViewModel) {
    var query by remember { mutableStateOf("") }
    var peerId by remember { mutableStateOf("") }
    var topK by remember { mutableStateOf("10") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Semantic query *") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = peerId, onValueChange = { peerId = it }, label = { Text("Peer ID (optional)") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = topK, onValueChange = { topK = it }, label = { Text("Top K") }, modifier = Modifier.width(80.dp), singleLine = true)
        }
        Button(
            onClick = { vm.query(query, peerId, topK.toIntOrNull() ?: 10) },
            enabled = query.isNotBlank() && !state.isQuerying,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isQuerying) { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
            Text("Search")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.queryResults, key = { it.conclusionId }) { c ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(c.content, style = MaterialTheme.typography.bodyMedium)
                        c.peerId?.let { Text("Peer: $it", style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}
