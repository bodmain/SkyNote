package com.example.note2.data

import android.util.Log
import com.example.note2.model.NoteModel
import com.example.note2.model.NotificationModel
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
    
    suspend fun insertNote(note: NoteModel) = noteDao.insert(note)
    
    suspend fun updateNote(note: NoteModel) = noteDao.update(note)
    
    suspend fun deleteNote(note: NoteModel) = noteDao.delete(note)

    // --- REALTIME SYNC ---
    fun startRealtimeSync(userId: String): Flow<Unit> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SYNC", "Listen failed.", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val remoteNote = change.document.toObject(NoteModel::class.java)
                    
                    launch {
                        val localNote = noteDao.getNoteById(remoteNote.id)
                        
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                if (localNote == null || remoteNote.timestamp > localNote.timestamp) {
                                    noteDao.insert(remoteNote)
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                noteDao.delete(remoteNote)
                            }
                        }
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun syncNoteToFirestore(note: NoteModel) {
        try {
            firestore.collection("users").document(note.userId)
                .collection("notes").document(note.id)
                .set(note).await()
        } catch (e: Exception) {
            Log.e("SYNC", "Error syncing to firestore: ${e.message}")
        }
    }

    suspend fun deleteNoteFromFirestore(noteId: String, userId: String) {
        try {
            firestore.collection("users").document(userId)
                .collection("notes").document(noteId)
                .delete().await()
        } catch (e: Exception) {
            Log.e("SYNC", "Error deleting from firestore: ${e.message}")
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
