package com.example.note2.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.note2.ui.screens.*
import com.example.note2.viewmodel.AuthViewModel
import com.example.note2.viewmodel.NoteViewModel
import com.example.note2.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun NavGraph(
    navController: NavHostController,
    noteViewModel: NoteViewModel,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    currentUser: FirebaseUser?,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = noteViewModel,
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                onAddNote = { navController.navigate(Screen.Detail.createRoute("-1")) },
                onEditNote = { note -> navController.navigate(Screen.Detail.createRoute(note.id)) },
                onNoteClick = { noteId -> navController.navigate(Screen.Detail.createRoute(noteId)) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = noteViewModel,
                onBack = { navController.popBackStack() },
                onNoteClick = { note -> 
                    navController.navigate(Screen.Detail.createRoute(note.id))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: "-1"
            NoteDetailScreen(
                noteId = noteId,
                viewModel = noteViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                currentUser = currentUser,
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                syncState = noteViewModel.syncState,
                onSyncClick = { noteViewModel.syncAllNotes() },
                onNavigateToTrash = { navController.navigate(Screen.Trash.route) },
                onLogout = {
                    authViewModel.logout()
                    noteViewModel.clearData()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Trash.route) {
            TrashScreen(
                viewModel = noteViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notification.route) {
            NotificationScreen(viewModel = noteViewModel, onBack = {
                navController.popBackStack()
            })
        }
    }
}
