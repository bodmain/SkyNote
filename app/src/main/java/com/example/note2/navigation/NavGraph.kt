package com.example.note2.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(400, easing = FastOutSlowInEasing)) + 
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            fadeOut(tween(400, easing = FastOutSlowInEasing)) + 
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            // Hiệu ứng Fade mượt mà khi quay lại màn hình trước
            fadeIn(tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400))
        },
        popExitTransition = {
            fadeOut(tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
        }
    ) {
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = {
                fadeOut(tween(800)) + scaleOut(targetScale = 0.85f, animationSpec = tween(800))
            }
        ) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            enterTransition = {
                if (initialState.destination.route == Screen.Splash.route) {
                    fadeIn(tween(1000)) + scaleIn(initialScale = 1.1f, animationSpec = tween(1000))
                } else {
                    fadeIn(tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
                }
            }
        ) {
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
                noteCountInTrash = noteViewModel.deletedNotes.size,
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
