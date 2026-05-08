package com.example.SkyNote.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SkyNote.data.model.ChecklistItem
import com.example.SkyNote.data.model.NoteModel
import com.example.SkyNote.data.model.NotificationModel
import com.example.SkyNote.data.repository.NoteRepository
import com.example.SkyNote.receiver.NoteNotificationReceiver
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withTimeout
import java.util.UUID

enum class SyncState { IDLE, SYNCING, SUCCESS, ERROR }

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    companion object {
        const val GUEST_USER_ID = "guest"
        private const val THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000
    }

    var notes by mutableStateOf<List<NoteModel>>(emptyList())
        private set

    var deletedNotes by mutableStateOf<List<NoteModel>>(emptyList())
        private set

    var syncState by mutableStateOf(SyncState.IDLE)
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
    private var deletedNotesJob: Job? = null
    private var syncJob: Job? = null
    private var notificationsJob: Job? = null
    
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    init {
        startObservingData()
        cleanOldDeletedNotes()
    }

    fun startObservingData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        startObservingNotes(userId)
        startObservingDeletedNotes(userId)
        startObservingNotifications(userId)
        if (userId != GUEST_USER_ID) {
            startRealtimeSync(userId)
        } else {
            syncJob?.cancel()
        }
    }

    private fun startObservingNotes(userId: String) {
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.getNotesByUser(userId).collectLatest { listOfNotes ->
                notes = listOfNotes
            }
        }
    }

    private fun startObservingDeletedNotes(userId: String) {
        deletedNotesJob?.cancel()
        deletedNotesJob = viewModelScope.launch {
            repository.getDeletedNotesByUser(userId).collectLatest { listOfNotes ->
                deletedNotes = listOfNotes
            }
        }
    }

    private fun startObservingNotifications(userId: String) {
        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            val twentyFourHoursAgo = System.currentTimeMillis() - 86400000
            try {
                repository.deleteOldNotifications(twentyFourHoursAgo)
            } catch (e: Exception) {
                Log.e("DATABASE", "Error deleting old notifications: ${e.message}")
            }
            repository.getNotificationsByUser(userId).collectLatest { list ->
                _notifications.value = list
            }
        }
    }

    private fun startRealtimeSync(userId: String) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            repository.startRealtimeSync(userId).collectLatest { }
        }
    }

    // Hàm hợp nhất để NoteDetailScreen gọi
    fun saveNote(context: Context, note: NoteModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        val noteToSave = note.copy(userId = userId, timestamp = System.currentTimeMillis())
        
        viewModelScope.launch {
            repository.insertNote(noteToSave)
            
            // Xử lý nhắc nhở
            if (noteToSave.reminderTime != null && noteToSave.reminderTime > System.currentTimeMillis() && !noteToSave.isDeleted) {
                NoteNotificationReceiver.scheduleNoteReminder(
                    context, noteToSave.id, noteToSave.title, noteToSave.description, noteToSave.reminderTime
                )
            } else {
                NoteNotificationReceiver.cancelNoteReminder(context, noteToSave.id)
            }

            if (userId != GUEST_USER_ID) {
                try { repository.syncNoteToFirestore(noteToSave) } catch (e: Exception) {}
            }
        }
    }

    fun syncAllNotes() {
        if (syncState != SyncState.IDLE) return
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            syncState = SyncState.ERROR
            viewModelScope.launch { delay(2000); syncState = SyncState.IDLE }
            return
        }
        viewModelScope.launch {
            syncState = SyncState.SYNCING
            try {
                withTimeout(15000) { repository.syncNotesToFirestore(notes, user.uid) }
                syncState = SyncState.SUCCESS
            } catch (e: Exception) {
                syncState = SyncState.ERROR
            } finally {
                withContext(NonCancellable) {
                    delay(2000)
                    syncState = SyncState.IDLE
                }
            }
        }
    }

    fun clearData() {
        notesJob?.cancel()
        deletedNotesJob?.cancel()
        syncJob?.cancel()
        notificationsJob?.cancel()
        notes = emptyList()
        deletedNotes = emptyList()
        _notifications.value = emptyList()
        syncState = SyncState.IDLE
        startObservingData()
    }

    fun addNote(title: String, description: String, color: Long = 0xFFFFFFFF, imagePath: String? = null, isChecklist: Boolean = false) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        val newNote = NoteModel(
            title = title,
            description = description,
            color = color,
            userId = userId,
            imagePath = imagePath,
            isChecklist = isChecklist
        )
        viewModelScope.launch {
            repository.insertNote(newNote)
            if (userId != GUEST_USER_ID) {
                try { repository.syncNoteToFirestore(newNote) } catch (e: Exception) {}
            }
        }
    }

    fun addChecklistNote() : String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        val newId = UUID.randomUUID().toString()
        val newNote = NoteModel(
            id = newId,
            title = "",
            description = "[ ] ",
            userId = userId,
            isChecklist = true
        )
        viewModelScope.launch {
            repository.insertNote(newNote)
            if (userId != GUEST_USER_ID) {
                try { repository.syncNoteToFirestore(newNote) } catch (e: Exception) {}
            }
        }
        return newId
    }

    fun updateNote(note: NoteModel) {
        val updatedNote = note.copy(timestamp = System.currentTimeMillis())
        viewModelScope.launch {
            repository.updateNote(updatedNote)
            if (updatedNote.userId != GUEST_USER_ID) {
                try { repository.syncNoteToFirestore(updatedNote) } catch (e: Exception) {}
            }
        }
    }

    fun deleteNote(note: NoteModel) {
        viewModelScope.launch {
            repository.deleteNote(note)
            if (note.userId != GUEST_USER_ID) {
                try { repository.deleteNoteFromFirestore(note.id, note.userId) } catch (e: Exception) {}
            }
        }
    }

    fun moveToTrash(context: Context, note: NoteModel) {
        viewModelScope.launch {
            val updatedNote = note.copy(isDeleted = true, timestamp = System.currentTimeMillis())
            repository.updateNote(updatedNote)
            NoteNotificationReceiver.cancelNoteReminder(context, note.id)
            if (updatedNote.userId != GUEST_USER_ID) {
                repository.syncNoteToFirestore(updatedNote)
            }
        }
    }

    fun restoreFromTrash(note: NoteModel) {
        viewModelScope.launch {
            val updatedNote = note.copy(isDeleted = false, timestamp = System.currentTimeMillis())
            repository.updateNote(updatedNote)
            if (updatedNote.userId != GUEST_USER_ID) {
                repository.syncNoteToFirestore(updatedNote)
            }
        }
    }

    fun permanentlyDeleteNote(context: Context, note: NoteModel) {
        viewModelScope.launch {
            repository.deleteNote(note)
            NoteNotificationReceiver.cancelNoteReminder(context, note.id)
            if (note.userId != GUEST_USER_ID) {
                try { repository.deleteNoteFromFirestore(note.id, note.userId) } catch (e: Exception) {}
            }
        }
    }

    fun emptyTrash(context: Context) {
        viewModelScope.launch { deletedNotes.forEach { permanentlyDeleteNote(context, it) } }
    }

    private fun cleanOldDeletedNotes() {
        viewModelScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - THIRTY_DAYS_IN_MILLIS
            deletedNotes.forEach { note ->
                if (note.timestamp < thirtyDaysAgo) { repository.deleteNote(note) }
            }
        }
    }

    fun filterNotes(query: String) {
        searchText = query
        searchResults = if (query.isEmpty()) emptyList()
        else notes.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }

    fun toggleSearch() {
        isSearchActive = !isSearchActive
        if (!isSearchActive) { searchText = ""; searchResults = emptyList() }
    }

    fun showDeleteDialog(note: NoteModel) { noteToDelete = note }
    fun dismissDeleteDialog() { noteToDelete = null }
    fun confirmDelete(context: Context) {
        noteToDelete?.let { moveToTrash(context, it) }
        noteToDelete = null
    }

    fun togglePin(context: Context, note: NoteModel) {
        val updatedNote = note.copy(isPinned = !note.isPinned, timestamp = System.currentTimeMillis())
        viewModelScope.launch {
            repository.updateNote(updatedNote)
            if (updatedNote.userId != GUEST_USER_ID) { repository.syncNoteToFirestore(updatedNote) }
        }
    }

    fun markAllNotificationsAsRead() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        viewModelScope.launch { repository.markAllAsRead(userId) }
    }

    fun markNotificationAsRead(notification: NotificationModel) {
        viewModelScope.launch { repository.updateNotification(notification.copy(isRead = true)) }
    }

    fun deleteNotification(notification: NotificationModel) {
        viewModelScope.launch { repository.deleteNotification(notification) }
    }
}
