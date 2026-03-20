package com.example.note2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.note2.auth.AuthRepository
import com.example.note2.auth.AuthViewModel
import com.example.note2.components_ui.LoginScreen
import com.example.note2.components_ui.RegisterScreen
import com.example.note2.data.AppDatabase
import com.example.note2.ui.NoteDetailScreen
import com.example.note2.ui_note.HomeScreen
import com.example.note2.viewmodel.NoteViewModel
import com.example.note2.ui.theme.Note2Theme

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Detail : Screen("detail/{noteId}") {
        fun createRoute(noteId: Int) = "detail/$noteId"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "note_database"
        )
            .fallbackToDestructiveMigration()
            .build()
        val dao = db.noteDao()
        val viewModel = NoteViewModel(dao)
        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)
        setContent {
            Note2Theme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen( authViewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Screen.Register.route)
                            }

                        )
                    }
                    composable(Screen.Register.route) {
                        RegisterScreen(
                            authViewModel = authViewModel,
                            onRegisterSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onAddNote = {
                                navController.navigate(Screen.Detail.createRoute(-1))
                            },
                            onEditNote = { note ->
                                navController.navigate(Screen.Detail.createRoute(note.id))
                            },
                            onNoteClick = { noteId ->
                                navController.navigate(Screen.Detail.createRoute(noteId))
                            }
                        )
                    }
                    composable(
                        route = Screen.Detail.route,
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                        NoteDetailScreen(
                            noteId = noteId,
                            viewModel = viewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}