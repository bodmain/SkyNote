package com.example.note2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.note2.auth.AuthRepository
import com.example.note2.auth.AuthViewModel
import com.example.note2.components_ui.LoginScreen
import com.example.note2.components_ui.NotificationScreen
import com.example.note2.components_ui.ProfileScreen
import com.example.note2.components_ui.RegisterScreen
import com.example.note2.components_ui.SplashScreen
import com.example.note2.data.AppDatabase
import com.example.note2.ui.NoteDetailScreen
import com.example.note2.ui_Screen.HomeScreen
import com.example.note2.viewmodel.NoteViewModel
import com.example.note2.ui.theme.Note2Theme
import com.google.firebase.messaging.FirebaseMessaging

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Detail : Screen("detail/{noteId}") {
        fun createRoute(noteId: Int) = "detail/$noteId"
    }
    object Profile : Screen("profile")
    object Notification : Screen("notification")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "note_database"
        )
            .fallbackToDestructiveMigration()
            .build()

        val dao = db.noteDao()
        val noteViewModel = NoteViewModel(dao)

        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "Token: $token")
            }


        setContent {
            Note2Theme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route
                ) {

                    //  Splash
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onNavigate = { route ->
                                if (route == Screen.Home.route) {
                                    noteViewModel.startObservingNotes()
                                }

                                navController.navigate(route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    //  Login
                    composable(Screen.Login.route) {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                noteViewModel.startObservingNotes()

                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Screen.Register.route)
                            }
                        )
                    }

                    //  Register
                    composable(Screen.Register.route) {
                        RegisterScreen(
                            authViewModel = authViewModel,
                            onRegisterSuccess = {
                                noteViewModel.startObservingNotes()

                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    //  Home
                    composable(Screen.Home.route) {
                        HomeScreen(
                            navController = navController,
                            viewModel = noteViewModel,
                            onAddNote = {
                                navController.navigate(Screen.Detail.createRoute(-1))
                            },
                            onEditNote = { note ->
                                navController.navigate(Screen.Detail.createRoute(note.id))
                            },
                            onNoteClick = { noteId ->
                                navController.navigate(Screen.Detail.createRoute(noteId))
                            },
                            onNavigateToProfile = {
                                navController.navigate(Screen.Profile.route)
                            }
                        )
                    }

                    //  Detail
                    composable(
                        route = Screen.Detail.route,
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1

                        NoteDetailScreen(
                            noteId = noteId,
                            viewModel = noteViewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    //profile
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            onLogout = {
                                authViewModel.logout()
                                noteViewModel.clearData()

                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0)
                                }
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    //notification
                    composable(Screen.Notification.route) {
                        NotificationScreen(viewModel = noteViewModel)

                    }
                }
            }
        }
    }
}