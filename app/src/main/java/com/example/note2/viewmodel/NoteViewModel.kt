package com.example.note2.viewmodel

import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class SyncState { IDLE, SYNCING, SUCCESS, ERROR }

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    companion object {
        const val GUEST_USER_ID = "guest"
    }

    var notes by mutableStateOf<List<NoteModel>>(emptyList())
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
    private var syncJob: Job? = null
    private var notificationsJob: Job? = null
    
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    init {
        startObservingData()
    }

    fun startObservingData() {
        startObservingNotes()
        startObservingNotifications()
        startRealtimeSync()
    }

    private fun startObservingNotes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.getNotesByUser(userId).collectLatest { listOfNotes ->
                notes = listOfNotes
            }
        }
    }

    private fun startObservingNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
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

    private fun startRealtimeSync() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            repository.startRealtimeSync(userId).collectLatest { }
        }
    }

    fun syncAllNotes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null || userId == GUEST_USER_ID) {
            syncState = SyncState.ERROR
            viewModelScope.launch {
                delay(2000)
                syncState = SyncState.IDLE
            }
            return
        }

        viewModelScope.launch {
            syncState = SyncState.SYNCING
            try {
                // Trong kiến trúc Offline-first mới, Realtime Sync đã lo việc tải dữ liệu từ Firestore về Room.
                // Ở đây ta chỉ cần đảm bảo tất cả Note local hiện tại đều được đẩy lên Firestore nếu có mạng.
                notes.forEach { note ->
                    repository.syncNoteToFirestore(note)
                }
                syncState = SyncState.SUCCESS
            } catch (e: Exception) {
                Log.e("SYNC", "Manual sync failed: ${e.message}")
                syncState = SyncState.ERROR
            } finally {
                delay(2000)
                syncState = SyncState.IDLE
            }
        }
    }

    fun clearData() {
        notesJob?.cancel()
        syncJob?.cancel()
        notificationsJob?.cancel()
        notes = emptyList()
        _notifications.value = emptyList()
        startObservingData()
    }

    fun addNote(title: String, description: String, color: Long = 0xFFFFFFFF, imagePath: String? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        val newNote = NoteModel(
            title = title, 
            description = description, 
            color = color, 
            userId = userId,
            imagePath = imagePath
        )
        
        viewModelScope.launch {
            // 1. Lưu Local trước (UI cập nhật ngay lập tức nhờ Flow)
            repository.insertNote(newNote)
            
            // 2. Đồng bộ lên Firestore nếu không phải khách
            if (userId != GUEST_USER_ID) {
                repository.syncNoteToFirestore(newNote)
            }
        }
    }

    fun updateNote(note: NoteModel) {
        val updatedNote = note.copy(timestamp = System.currentTimeMillis())
        viewModelScope.launch {
            // 1. Cập nhật Local
            repository.updateNote(updatedNote)
            
            // 2. Cập nhật Firestore
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && userId != GUEST_USER_ID) {
                repository.syncNoteToFirestore(updatedNote)
            }
        }
    }

    fun deleteNote(note: NoteModel) {
        viewModelScope.launch {
            // 1. Xóa Local
            repository.deleteNote(note)
            
            // 2. Xóa Firestore
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && userId != GUEST_USER_ID) {
                repository.deleteNoteFromFirestore(note.id, userId)
            }
        }
    }

    fun filterNotes(query: String) {
        searchText = query
        searchResults = if (query.isEmpty()) {
            emptyList()
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }

    fun toggleSearch() {
        isSearchActive = !isSearchActive
        if (!isSearchActive) {
            searchText = ""
            searchResults = emptyList()
        }
    }

    fun showDeleteDialog(note: NoteModel) {
        noteToDelete = note
    }

    fun dismissDeleteDialog() {
        noteToDelete = null
    }

    fun confirmDelete() {
        noteToDelete?.let { note ->
            deleteNote(note)
        }
        noteToDelete = null
    }

    fun markNotificationAsRead(notification: NotificationModel) {
        viewModelScope.launch { repository.updateNotification(notification.copy(isRead = true)) }
    }

    fun markAllNotificationsAsRead() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_USER_ID
        viewModelScope.launch { repository.markAllAsRead(userId) }
    }

    fun deleteNotification(notification: NotificationModel) {
        viewModelScope.launch { repository.deleteNotification(notification) }
    }
}
