package com.example.SkyNote.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String = "", val icon: ImageVector? = null) {
    object Splash : Screen("splash")
    object Home : Screen("home", "Ghi chú", Icons.Default.Home)
    object Search : Screen("search")
    object Notification : Screen("notification", "Thông báo", Icons.Default.Notifications)
    object Profile : Screen("profile", "Cá nhân", Icons.Default.Person)
    object Trash : Screen("trash", "Thùng rác", Icons.Default.Delete)
    
    object Detail : Screen("detail/{noteId}") {
        fun createRoute(noteId: String) = "detail/$noteId"
    }
}
