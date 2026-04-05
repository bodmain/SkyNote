package com.example.note2

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.note2.auth.AuthRepository
import com.example.note2.auth.AuthViewModel
import com.example.note2.components_ui.NotificationScreen
import com.example.note2.components_ui.ProfileScreen
import com.example.note2.components_ui.SplashScreen
import com.example.note2.data.AppDatabase
import com.example.note2.data.NoteRepository
import com.example.note2.receiver.DailyNotificationReceiver
import com.example.note2.ui.NoteDetailScreen
import com.example.note2.ui_Screen.HomeScreen
import com.example.note2.viewmodel.NoteViewModel
import com.example.note2.ui.theme.Note2Theme
import com.example.note2.ui.theme.ThemeManager
import com.example.note2.viewmodel.ThemeViewModel
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
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
        val themeManager = ThemeManager(applicationContext)
        val themeViewModel = ThemeViewModel(themeManager)

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(db.noteDao(), db.notificationDao())
        val noteViewModel = NoteViewModel(repository)

        val authRepository = AuthRepository()
        val authViewModel = AuthViewModel(authRepository)

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "Token: $token")
            }

        scheduleDailyNotification(this)

        setContent {
            val currentTheme by themeViewModel.themeMode.collectAsState()
            Note2Theme(themeMode = currentTheme) {
                val navController = rememberNavController()


                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route

                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onNavigate = {
                                navController.navigate(Screen.Home.route) {
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

                    composable(
                        route = Screen.Detail.route,
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                        NoteDetailScreen(
                            noteId = noteId,
                            viewModel = noteViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            onLogout = {
                                authViewModel.logout()
                                noteViewModel.clearData()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0)
                                }
                            },
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
        }
    }

    private fun scheduleDailyNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 23)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
