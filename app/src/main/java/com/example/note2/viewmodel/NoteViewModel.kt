package com.example.note2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note2.data.NoteDao
import com.example.note2.model.NoteModel
import kotlinx.coroutines.launch

class NoteViewModel(private val dao: NoteDao) : ViewModel() {

    var notes by mutableStateOf<List<NoteModel>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            dao.getAllNotes().collect { listOfNotes ->
                notes = listOfNotes
            }
        }
    }

    fun addNote(title: String, description: String) {
        if (title.isBlank() && description.isBlank()) return

        viewModelScope.launch {
            val newNote = NoteModel(
                title = title,
                description = description,
                timestamp = System.currentTimeMillis()
            )
            dao.insert(newNote)
        }
    }

    fun updateNote(note: NoteModel) {
        viewModelScope.launch {
            val updatedNote = note.copy(timestamp = System.currentTimeMillis())
            dao.update(updatedNote)
        }
    }

    fun deleteNote(note: NoteModel) {
        viewModelScope.launch {
            dao.delete(note)
        }
    }
}