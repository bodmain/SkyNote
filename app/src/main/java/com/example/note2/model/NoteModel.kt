package com.example.note2.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity("notes")
data class NoteModel(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val color: Long = 0xFFFFFFFF,
    val userId: String = "",
    val imagePath: String? = null
)
