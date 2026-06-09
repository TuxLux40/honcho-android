package dev.honcho.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.honcho.android.ui.conclusions.ConclusionsScreen
import dev.honcho.android.ui.keys.KeysScreen
import dev.honcho.android.ui.peer.PeerDetailScreen
import dev.honcho.android.ui.session.SessionDetailScreen
import dev.honcho.android.ui.setup.SetupScreen
import dev.honcho.android.ui.webhooks.WebhooksScreen
import dev.honcho.android.ui.workspace.WorkspaceDetailScreen
import dev.honcho.android.ui.workspaces.WorkspacesScreen
import kotlinx.serialization.Serializable

@Serializable object Setup
@Serializable object Workspaces
@Serializable data class WorkspaceDetail(val workspaceId: String)
@Serializable data class PeerDetail(val workspaceId: String, val peerId: String)
@Serializable data class SessionDetail(val workspaceId: String, val sessionId: String)
@Serializable data class Conclusions(val workspaceId: String)
@Serializable data class Webhooks(val workspaceId: String)
@Serializable object Keys

@Composable
fun HonchoNavHost(
    navController: NavHostController,
    startDestination: Any
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable<Setup> {
            SetupScreen(onSetupComplete = {
                navController.navigate(Workspaces) { popUpTo(0) }
            })
        }
        composable<Workspaces> {
            WorkspacesScreen(
                onWorkspaceClick = { navController.navigate(WorkspaceDetail(it)) },
                onSettingsClick = { navController.navigate(Setup) },
                onKeysClick = { navController.navigate(Keys) }
            )
        }
        composable<WorkspaceDetail> { entry ->
            val route = entry.toRoute<WorkspaceDetail>()
            WorkspaceDetailScreen(
                workspaceId = route.workspaceId,
                onPeerClick = { navController.navigate(PeerDetail(route.workspaceId, it)) },
                onSessionClick = { navController.navigate(SessionDetail(route.workspaceId, it)) },
                onConclusionsClick = { navController.navigate(Conclusions(route.workspaceId)) },
                onWebhooksClick = { navController.navigate(Webhooks(route.workspaceId)) },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable<PeerDetail> { entry ->
            val route = entry.toRoute<PeerDetail>()
            PeerDetailScreen(
                workspaceId = route.workspaceId,
                peerId = route.peerId,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable<SessionDetail> { entry ->
            val route = entry.toRoute<SessionDetail>()
            SessionDetailScreen(
                workspaceId = route.workspaceId,
                sessionId = route.sessionId,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable<Conclusions> { entry ->
            val route = entry.toRoute<Conclusions>()
            ConclusionsScreen(
                workspaceId = route.workspaceId,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable<Webhooks> { entry ->
            val route = entry.toRoute<Webhooks>()
            WebhooksScreen(
                workspaceId = route.workspaceId,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable<Keys> {
            KeysScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}
