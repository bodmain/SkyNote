package com.example.note2.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("NoteModel")
data class NoteModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val color: Long = 0xFFFFFFFF
)



