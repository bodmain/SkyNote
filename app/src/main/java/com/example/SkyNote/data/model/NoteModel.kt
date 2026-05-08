package com.example.SkyNote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity("notes")
data class NoteModel(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val color: Long = 0xFFFFFFFF,
    val userId: String = "",
    val imagePath: String? = null,
    val isChecklist: Boolean = false,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val reminderTime: Long? = null,
    val isReminded: Boolean = false,
    val labels: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList()
)

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)
