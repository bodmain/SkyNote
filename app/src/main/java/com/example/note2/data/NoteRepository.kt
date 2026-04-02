package com.example.note2.data

import com.example.note2.model.NoteModel
import com.example.note2.model.NotificationModel
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val notificationDao: NotificationDao
) {
    // Notes
    fun getNotesByUser(userId: String): Flow<List<NoteModel>> = noteDao.getNotesByUser(userId)
    
    suspend fun insertNote(note: NoteModel) = noteDao.insert(note)
    
    suspend fun updateNote(note: NoteModel) = noteDao.update(note)
    
    suspend fun deleteNote(note: NoteModel) = noteDao.delete(note)

    // Notifications
    fun getNotificationsByUser(userId: String): Flow<List<NotificationModel>> = 
        notificationDao.getNotificationsByUser(userId)
        
    suspend fun insertNotification(notification: NotificationModel) = 
        notificationDao.insert(notification)
        
    suspend fun updateNotification(notification: NotificationModel) = 
        notificationDao.update(notification)

    suspend fun deleteNotification(notification: NotificationModel) = 
        notificationDao.delete(notification)
        
    suspend fun deleteAllNotifications(userId: String) = 
        notificationDao.deleteAllNotifications(userId)

    suspend fun markAllAsRead(userId: String) = 
        notificationDao.markAllAsRead(userId)

    suspend fun deleteOldNotifications(expiryTime: Long) = 
        notificationDao.deleteOldNotifications(expiryTime)
}