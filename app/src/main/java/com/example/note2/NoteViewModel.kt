package com.example.note2

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoteViewModel(private val dao: NoteDao) : ViewModel() {
    var notes = mutableStateListOf<NoteModel>()
        private set

    fun loadNotes() {
        viewModelScope.launch {
            notes.clear()
            notes.addAll(dao.getAllNotes())
        }
    }

    fun addNote(title: String, description: String) {
        viewModelScope.launch {
            val newNote = NoteModel(title = title, description = description)
            dao.insert(newNote)
            loadNotes()
        }
    }

    fun deleteNote(note: NoteModel) {
        viewModelScope.launch {
            dao.delete(note)
            loadNotes()
        }

    }
    fun updateNote(note: NoteModel) {
        viewModelScope.launch {
            dao.update(note)
            loadNotes()
        }
    }
}
