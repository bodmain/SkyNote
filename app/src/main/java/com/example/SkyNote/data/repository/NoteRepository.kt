package com.example.SkyNote.data.repository

import android.util.Log
import com.example.SkyNote.data.local.NoteDao
import com.example.SkyNote.data.local.NotificationDao
import com.example.SkyNote.data.model.NoteModel
import com.example.SkyNote.data.model.NotificationModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NoteRepository(
    private val noteDao: NoteDao,
    private val notificationDao: NotificationDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    fun getNotesByUser(userId: String): Flow<List<NoteModel>> = noteDao.getNotesByUser(userId)
    fun getDeletedNotesByUser(userId: String): Flow<List<NoteModel>> = noteDao.getDeletedNotesByUser(userId)
    suspend fun insertNote(note: NoteModel) = noteDao.insert(note)
    suspend fun updateNote(note: NoteModel) = noteDao.update(note)
    suspend fun deleteNote(note: NoteModel) = noteDao.delete(note)

    fun startRealtimeSync(userId: String): Flow<Unit> = callbackFlow {
        if (userId == "guest") { close(); return@callbackFlow }
        val listener = firestore.collection("users").document(userId)
            .collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.documentChanges?.forEach { change ->
                    try {
                        val remoteNote = change.document.toObject(NoteModel::class.java).copy(id = change.document.id)
                        launch {
                            val localNote = noteDao.getNoteById(remoteNote.id)
                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    if (localNote == null || remoteNote.timestamp > localNote.timestamp) {
                                        noteDao.insert(remoteNote)
                                    }
                                }
                                DocumentChange.Type.REMOVED -> { noteDao.delete(remoteNote) }
                            }
                        }
                    } catch (e: Exception) { Log.e("SYNC", "Error: ${e.message}") }
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun syncNoteToFirestore(note: NoteModel) {
        if (note.userId == "guest") return
        try {
            firestore.collection("users").document(note.userId)
                .collection("notes").document(note.id)
                .set(note).await()
        } catch (e: Exception) {
            Log.e("SYNC", "Sync failed: ${e.message}")
            // Không throw e để tránh văng app khi mất mạng hoặc sai SHA-1
        }
    }

    suspend fun syncNotesToFirestore(notes: List<NoteModel>, userId: String) {
        if (userId == "guest" || notes.isEmpty()) return
        try {
            notes.chunked(500).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { note ->
                    val docRef = firestore.collection("users").document(userId).collection("notes").document(note.id)
                    batch.set(docRef, note)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Batch sync failed: ${e.message}")
            throw e // Ném lỗi cho hàm gọi (syncAllNotes) xử lý để hiện UI báo lỗi
        }
    }

    suspend fun deleteNoteFromFirestore(noteId: String, userId: String) {
        if (userId == "guest") return
        try {
            firestore.collection("users").document(userId).collection("notes").document(noteId).delete().await()
        } catch (e: Exception) {
            Log.e("SYNC", "Delete firestore failed: ${e.message}")
        }
    }

    // Notifications
    fun getNotificationsByUser(userId: String): Flow<List<NotificationModel>> = notificationDao.getNotificationsByUser(userId)
    suspend fun insertNotification(notification: NotificationModel) = notificationDao.insert(notification)
    suspend fun updateNotification(notification: NotificationModel) = notificationDao.update(notification)
    suspend fun deleteNotification(notification: NotificationModel) = notificationDao.delete(notification)
    suspend fun markAllAsRead(userId: String) = notificationDao.markAllAsRead(userId)
    suspend fun deleteOldNotifications(expiryTime: Long) = notificationDao.deleteOldNotifications(expiryTime)
}
