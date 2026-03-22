package com.example.note2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note2.data.NoteDao
import com.example.note2.model.NoteModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
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

    private var notesJob: Job? = null

    init {
        startObservingNotes()
    }

    // Hàm này dùng để bắt đầu lắng nghe note của user hiện tại
    fun startObservingNotes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            notesJob?.cancel() // Huỷ lắng nghe cũ nếu có
            notesJob = viewModelScope.launch {
                dao.getNotesByUser(userId).collectLatest { listOfNotes ->
                    notes = listOfNotes
                }
            }
        } else {
            notes = emptyList() // Nếu không có user, xóa danh sách
        }
    }

    // Hàm xóa dữ liệu khi Logout
    fun clearData() {
        notesJob?.cancel()
        notesJob = null
        notes = emptyList()
        searchText = ""
        isSearchActive = false
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
    fun addNote(title: String, description: String, color: Long = 0xFFFFFFFF) {
        if (title.isBlank() && description.isBlank()) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Đảm bảo luôn lắng nghe đúng user trước khi add
        if (notesJob == null || !notesJob!!.isActive) {
            startObservingNotes()
        }

        viewModelScope.launch {
            val newNote = NoteModel(
                title = title,
                description = description,
                timestamp = System.currentTimeMillis(),
                color = color,
                userId = userId
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

    // delele note
    fun deleteNote(note: NoteModel) {
        viewModelScope.launch {
            dao.delete(note)
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

    fun updateNoteColor(noteId: Int, newColor: Long) {
        viewModelScope.launch {
            val noteToUpdate = notes.find { it.id == noteId }
            noteToUpdate?.let {
                dao.update(it.copy(color = newColor))
            }
        }
    }
}
