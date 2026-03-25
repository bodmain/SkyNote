package com.example.note2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note2.data.NoteRepository
import com.example.note2.model.NoteModel
import com.example.note2.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

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

    private var notesJob: Job? = null
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    init {
        startObservingNotes()
        startObservingNotifications()
    }

    fun startObservingNotes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            notesJob?.cancel()
            notesJob = viewModelScope.launch {
                repository.getNotesByUser(userId).collectLatest { listOfNotes ->
                    notes = listOfNotes
                }
            }
        } else {
            notes = emptyList()
        }
    }

    fun startObservingNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getNotificationsByUser(userId).collectLatest { list ->
                _notifications.value = list
            }
        }
    }

    fun clearData() {
        notesJob?.cancel()
        notesJob = null
        notes = emptyList()
        searchText = ""
        isSearchActive = false
    }

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

    fun addNote(title: String, description: String, color: Long = 0xFFFFFFFF) {
        if (title.isBlank() && description.isBlank()) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            val newNote = NoteModel(
                title = title,
                description = description,
                timestamp = System.currentTimeMillis(),
                color = color,
                userId = userId
            )
            repository.insertNote(newNote)
        }
    }

    fun updateNote(note: NoteModel) {
        viewModelScope.launch {
            repository.updateNote(note.copy(timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: NoteModel) {
        viewModelScope.launch {
            repository.deleteNote(note)
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

    fun addNotification(title: String, message: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val newNotification = NotificationModel(
                title = title,
                message = message,
                userId = userId
            )
            repository.insertNotification(newNotification)
        }
    }
}
