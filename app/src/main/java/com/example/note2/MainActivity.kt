package com.example.note2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import com.example.note2.ui.theme.Note2Theme
import androidx.compose.material3.Surface
import androidx.room.Room


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "note_database"
        ).build()

        val dao = db.noteDao()
        val viewModel = NoteViewModel(dao)

        setContent {
            HomeScreen(viewModel)
        }


        viewModel.loadNotes()
    }
}

