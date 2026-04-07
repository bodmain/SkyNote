package com.example.note2.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: String = "", // Thêm trường noteId để liên kết với NoteModel
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val isRead: Boolean = false
)
