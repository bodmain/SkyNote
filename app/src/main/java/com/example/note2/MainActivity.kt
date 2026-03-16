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
import com.example.note2.data.AppDatabase
import com.example.note2.ui.NoteDetailScreen
import com.example.note2.ui_note.HomeScreen
import com.example.note2.viewmodel.NoteViewModel
import com.example.note2.ui.theme.Note2Theme

sealed class Screen(val route: String) {
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
            .fallbackToDestructiveMigration(false) // Thêm dòng này để tự động xóa DB cũ khi đổi schema
        .build()

        val dao = db.noteDao()
        val viewModel = NoteViewModel(dao)

        setContent {
            Note2Theme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onAddNote = {
                                navController.navigate(Screen.Detail.createRoute(-1))
                            },
                            onEditNote = { note ->
                                navController.navigate(Screen.Detail.createRoute(note.id))
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