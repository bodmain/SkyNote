package com.example.note2.viewmodel

import android.R.attr.text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note2.data.NoteDao
import com.example.note2.model.NoteModel
import kotlinx.coroutines.launch

class NoteViewModel(private val dao: NoteDao) : ViewModel() {

    var notes by mutableStateOf<List<NoteModel>>(emptyList())
        private set
    var isSearchActive by mutableStateOf(false)
        private set
    var searchText by mutableStateOf("")
        private set
    var searchResults by mutableStateOf<List<NoteModel>>(emptyList())
        private set
    var noteToDelete by mutableStateOf<NoteModel?>(null)
        private set


    init {
        viewModelScope.launch {
            dao.getAllNotes().collect { listOfNotes ->
                notes = listOfNotes
            }
        }
    }

    // search note
    fun filterNotes(text: String) {
        searchText = text
        searchResults = if (text.isBlank()) {
            emptyList()
        } else {
            notes.filter { it.title.contains(text, ignoreCase = true) }
        }
    }

    fun toggleSearch() {
        isSearchActive = !isSearchActive
        if (!isSearchActive) {
            searchText = ""
            searchResults = emptyList()
        }
    }

    // add note
    fun addNote(title: String, description: String,color: Long = 0xFFFFFFFF) {
        if (title.isBlank() && description.isBlank()) return

        viewModelScope.launch {
            val newId = if (notes.isEmpty()) 1 else notes.last().id + 1
            val newNote = NoteModel(
                title = title,
                description = description,
                timestamp = System.currentTimeMillis(),
                color = color

            )
            notes = notes + newNote
            dao.insert(newNote)
        }
    }

    fun updateNote(note: NoteModel) {
        viewModelScope.launch {
            val updatedNote = note.copy(timestamp = System.currentTimeMillis())
            dao.update(updatedNote)
        }
    }

    // delele note
    fun deleteNote(note: NoteModel) {
        viewModelScope.launch {
            dao.delete(note)
        }
        notes = notes.filter { it.id != note.id }
        if (isSearchActive) {
            filterNotes(searchText)

        }
    }

    fun dismissDeleteDialog() {
        noteToDelete = null
    }

    fun confirmDelete() {
        noteToDelete?.let {
            deleteNote(it)
            noteToDelete = null
        }

    }

    fun showDeleteDialog(note: NoteModel) {
        noteToDelete = note
    }

    //
    fun updateNoteColor(noteId: Int, newColor: Long) {
        notes = notes.map {
            if (it.id == noteId) {
                it.copy(color = newColor)
            } else {
                it
            }
        }
    }
}