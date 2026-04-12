package com.example.note2

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.note2.data.local.AppDatabase
import com.example.note2.data.repository.AuthRepository
import com.example.note2.data.repository.NoteRepository
import com.example.note2.navigation.NavGraph
import com.example.note2.navigation.Screen
import com.example.note2.receiver.NoteNotificationReceiver
import com.example.note2.ui.theme.Note2Theme
import com.example.note2.ui.theme.ThemeManager
import com.example.note2.viewmodel.AuthViewModel
import com.example.note2.viewmodel.NoteViewModel
import com.example.note2.viewmodel.ThemeViewModel
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

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
            val currentUser by authViewModel.currentUser.collectAsState()

            Note2Theme(themeMode = currentTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomNavScreens = listOf(Screen.Home.route, Screen.Trash.route, Screen.Profile.route)
                // Đảm bảo không hiện BottomBar ở màn hình Splash hoặc các màn hình không thuộc main flow
                val showBottomNav = currentDestination?.route in bottomNavScreens

                Scaffold(
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp
                            ) {
                                listOf(Screen.Home, Screen.Trash, Screen.Profile).forEach { screen ->
                                    NavigationBarItem(
                                        icon = {
                                            if (screen == Screen.Profile && currentUser?.photoUrl != null) {
                                                AsyncImage(
                                                    model = currentUser!!.photoUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                screen.icon?.let { Icon(it, contentDescription = null) }
                                            }
                                        },
                                        label = { Text(screen.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        noteViewModel = noteViewModel,
                        authViewModel = authViewModel,
                        themeViewModel = themeViewModel,
                        currentUser = currentUser,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun scheduleDailyNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NoteNotificationReceiver::class.java).apply {
            action = NoteNotificationReceiver.ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
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
