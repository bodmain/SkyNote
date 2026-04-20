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

    // --- NOTES (LOCAL) ---
    fun getNotesByUser(userId: String): Flow<List<NoteModel>> = noteDao.getNotesByUser(userId)
    
    fun getDeletedNotesByUser(userId: String): Flow<List<NoteModel>> = noteDao.getDeletedNotesByUser(userId)
    
    suspend fun insertNote(note: NoteModel) = noteDao.insert(note)
    
    suspend fun updateNote(note: NoteModel) = noteDao.update(note)
    
    suspend fun deleteNote(note: NoteModel) = noteDao.delete(note)

    // --- REALTIME SYNC ---
    fun startRealtimeSync(userId: String): Flow<Unit> = callbackFlow {
        if (userId == "guest") {
            close()
            return@callbackFlow
        }
        
        Log.d("SYNC", "Starting realtime sync for user: $userId")
        val listener = firestore.collection("users").document(userId)
            .collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SYNC", "Listen failed.", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    try {
                        val remoteNote = change.document.toObject(NoteModel::class.java).copy(id = change.document.id)
                        
                        launch {
                            val localNote = noteDao.getNoteById(remoteNote.id)
                            
                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    if (localNote == null || remoteNote.timestamp > localNote.timestamp) {
                                        noteDao.insert(remoteNote)
                                        Log.d("SYNC", "Updated local note: ${remoteNote.id}")
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    noteDao.delete(remoteNote)
                                    Log.d("SYNC", "Deleted local note: ${remoteNote.id}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SYNC", "Error processing remote change: ${e.message}")
                    }
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
            Log.d("SYNC", "Synced note to Firestore: ${note.id}")
        } catch (e: Exception) {
            Log.e("SYNC", "Error syncing to Firestore: ${e.message}")
            throw e
        }
    }

    suspend fun syncNotesToFirestore(notes: List<NoteModel>, userId: String) {
        if (userId == "guest" || notes.isEmpty()) return
        
        try {
            notes.chunked(500).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { note ->
                    val docRef = firestore.collection("users").document(userId)
                        .collection("notes").document(note.id)
                    batch.set(docRef, note)
                }
                batch.commit().await()
            }
            Log.d("SYNC", "Batch sync completed for ${notes.size} notes")
        } catch (e: Exception) {
            Log.e("SYNC", "Batch sync failed: ${e.message}")
            throw e
        }
    }

    suspend fun deleteNoteFromFirestore(noteId: String, userId: String) {
        if (userId == "guest") return
        try {
            firestore.collection("users").document(userId)
                .collection("notes").document(noteId)
                .delete().await()
        } catch (e: Exception) {
            Log.e("SYNC", "Error deleting from Firestore: ${e.message}")
            throw e
        }
    }

    // --- NOTIFICATIONS ---
    fun getNotificationsByUser(userId: String): Flow<List<NotificationModel>> = 
        notificationDao.getNotificationsByUser(userId)
        
    suspend fun insertNotification(notification: NotificationModel) = 
        notificationDao.insert(notification)
        
    suspend fun updateNotification(notification: NotificationModel) = 
        notificationDao.update(notification)

    suspend fun deleteNotification(notification: NotificationModel) = 
        notificationDao.delete(notification)
    
    suspend fun markAllAsRead(userId: String) = 
        notificationDao.markAllAsRead(userId)

    suspend fun deleteOldNotifications(expiryTime: Long) = 
        notificationDao.deleteOldNotifications(expiryTime)
}
