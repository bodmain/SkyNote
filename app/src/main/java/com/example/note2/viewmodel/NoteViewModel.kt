package com.example.note2.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note2.data.model.ChecklistItem
import com.example.note2.data.model.NoteModel
import com.example.note2.data.model.NotificationModel
import com.example.note2.data.repository.NoteRepository
import com.example.note2.receiver.NoteNotificationReceiver
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

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

    var noteToDelete by mutableStateOf<NoteModel?>(null)
        private set

    private var notesJob: Job? = null
    private var deletedNotesJob: Job? = null
    private var syncJob: Job? = null
    private var notificationsJob: Job? = null
    
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        startObservingData(currentUserId)
        cleanOldDeletedNotes()
    }

    fun startObservingData(userId: String) {
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
                val notesToSync = notes.map { it.copy(userId = user.uid) }
                withTimeout(15000) { repository.syncNotesToFirestore(notesToSync, user.uid) }
                syncState = SyncState.SUCCESS
            } catch (e: Exception) {
                syncState = SyncState.ERROR
            } finally {
                delay(2000)
                syncState = SyncState.IDLE
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
    }

    fun saveNote(context: Context, note: NoteModel) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: GUEST_USER_ID
        val noteToSave = note.copy(userId = userId, timestamp = System.currentTimeMillis())
        
        viewModelScope.launch {
            repository.insertNote(noteToSave)
            
            if (noteToSave.reminderTime != null && noteToSave.reminderTime > System.currentTimeMillis() && !noteToSave.isDeleted) {
                NoteNotificationReceiver.scheduleNoteReminder(
                    context,
                    noteToSave.id,
                    noteToSave.title.ifBlank { "Ghi chú của bạn" },
                    noteToSave.description.ifBlank { "Nhấp để xem chi tiết" },
                    noteToSave.reminderTime
                )
            } else {
                NoteNotificationReceiver.cancelNoteReminder(context, noteToSave.id)
            }

            if (userId != GUEST_USER_ID) {
                try { repository.syncNoteToFirestore(noteToSave) } catch (e: Exception) {}
            }
        }
    }

    fun moveToTrash(context: Context, note: NoteModel) {
        viewModelScope.launch {
            val updatedNote = note.copy(isDeleted = true, timestamp = System.currentTimeMillis())
            repository.updateNote(updatedNote)
            NoteNotificationReceiver.cancelNoteReminder(context, note.id)

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && userId != GUEST_USER_ID) {
                repository.syncNoteToFirestore(updatedNote)
            }
        }
    }

    fun restoreFromTrash(note: NoteModel) {
        viewModelScope.launch {
            val updatedNote = note.copy(isDeleted = false, timestamp = System.currentTimeMillis())
            repository.updateNote(updatedNote)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && userId != GUEST_USER_ID) {
                repository.syncNoteToFirestore(updatedNote)
            }
        }
    }

    fun permanentlyDeleteNote(context: Context, note: NoteModel) {
        viewModelScope.launch {
            repository.deleteNote(note)
            NoteNotificationReceiver.cancelNoteReminder(context, note.id)

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && userId != GUEST_USER_ID) {
                try { repository.deleteNoteFromFirestore(note.id, userId) } catch (e: Exception) {}
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
                if (note.timestamp < thirtyDaysAgo) { 
                    repository.deleteNote(note)
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null && userId != GUEST_USER_ID) {
                        try { repository.deleteNoteFromFirestore(note.id, userId) } catch (e: Exception) {}
                    }
                }
            }
        }
    }

    fun showDeleteDialog(note: NoteModel) { noteToDelete = note }
    fun dismissDeleteDialog() { noteToDelete = null }
    fun confirmDelete(context: Context) {
        noteToDelete?.let { moveToTrash(context, it) }
        noteToDelete = null
    }

    fun togglePin(context: Context, note: NoteModel) {
        saveNote(context, note.copy(isPinned = !note.isPinned))
    }

    fun updateColor(context: Context, note: NoteModel, color: Long) {
        saveNote(context, note.copy(color = color))
    }

    fun updateReminder(context: Context, note: NoteModel, time: Long?) {
        saveNote(context, note.copy(reminderTime = time))
    }

    fun updateLabels(context: Context, note: NoteModel, labels: List<String>) {
        saveNote(context, note.copy(labels = labels))
    }

    fun updateChecklist(context: Context, note: NoteModel, checklist: List<ChecklistItem>) {
        saveNote(context, note.copy(checklist = checklist))
    }

    fun markAllNotificationsAsRead() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        viewModelScope.launch {
            repository.markAllAsRead(userId)
        }
    }

    fun markNotificationAsRead(notification: NotificationModel) {
        viewModelScope.launch {
            repository.updateNotification(notification.copy(isRead = true))
        }
    }

    fun deleteNotification(notification: NotificationModel) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }
}
