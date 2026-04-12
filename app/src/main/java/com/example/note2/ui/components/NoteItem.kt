package com.example.note2.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.note2.data.model.NoteModel
import com.example.note2.ui.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteItem(
    note: NoteModel,
    onClick: (NoteModel) -> Unit,
    onDelete: (NoteModel) -> Unit
) {
    val backgroundColor = remember(note.color, note.id) {
        if (note.color == 0xFFFFFFFF || note.color == 0L) {
            NoteColors[note.id.hashCode().coerceAtLeast(0) % NoteColors.size]
        } else {
            Color(note.color)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onClick(note) },
                onLongClick = { onDelete(note) }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (note.labels.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.labels.take(3).forEach { label ->
                        Surface(
                            color = Color.Black.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (note.labels.size > 3) {
                        Text(
                            text = "...",
                            fontSize = 10.sp,
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (note.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Black.copy(alpha = 0.6f),
                        lineHeight = 18.sp
                    ),
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (note.checklist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                note.checklist.take(3).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (item.isChecked) Color.Black.copy(alpha = 0.2f) 
                                    else Color.Transparent, 
                                    CircleShape
                                )
                                .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Black.copy(alpha = if (item.isChecked) 0.3f else 0.6f),
                                textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (note.checklist.size > 3) {
                    Text(
                        text = "+ ${note.checklist.size - 3} mục khác",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Black.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getRelativeTime(note.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Black.copy(alpha = 0.3f),
                        fontSize = 9.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (note.reminderTime != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Reminder",
                            tint = if (note.isReminded) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (note.isReminded) "Đã nhắc" else SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(note.reminderTime)),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (note.isReminded) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                textDecoration = if (note.isReminded) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (note.labels.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = "Labels",
                        tint = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    if (diff < 0) return "Vừa xong"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    if (minutes < 1) return "Vừa xong"
    if (minutes < 60) return "$minutes phút trước"
    val noteCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCalendar = Calendar.getInstance().apply { timeInMillis = now }
    val isSameDay = noteCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
            noteCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR)
    return if (isSameDay) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } else {
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}
