package com.example.note2.components_ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.note2.model.NoteModel
import com.example.note2.ui.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: NoteModel,
    isGalleryMode: Boolean = false, // Thêm chế độ Gallery
    onClick: (NoteModel) -> Unit,
    onDelete: (NoteModel) -> Unit
) {
    val backgroundColor = remember(note.id) {
        NoteColors[note.id.hashCode().coerceAtLeast(0) % NoteColors.size]
    }

    var showDeleteIcon by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(if (isGalleryMode) 12.dp else 16.dp))
            .combinedClickable(
                onClick = { onClick(note) },
                onLongClick = { showDeleteIcon = !showDeleteIcon }
            ),
        shape = RoundedCornerShape(if (isGalleryMode) 12.dp else 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column {
            if (note.imagePath != null) {
                AsyncImage(
                    model = note.imagePath,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (isGalleryMode) 1f else 1.5f) // Vuông ở Gallery, 16:10 ở chế độ thường
                        .clip(RoundedCornerShape(
                            topStart = if (isGalleryMode) 12.dp else 16.dp, 
                            topEnd = if (isGalleryMode) 12.dp else 16.dp
                        )),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(if (isGalleryMode) 8.dp else 12.dp)
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.9f),
                            fontSize = if (isGalleryMode) 13.sp else 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Chỉ hiện mô tả nếu không phải chế độ Gallery
                if (!isGalleryMode && note.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Black.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        ),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(if (isGalleryMode) 4.dp else 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getRelativeTime(note.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Black.copy(alpha = 0.3f),
                            fontSize = 9.sp
                        )
                    )

                    if (showDeleteIcon) {
                        IconButton(
                            onClick = { 
                                onDelete(note)
                                showDeleteIcon = false
                            },
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    } else {
                        Box(modifier = Modifier.size(3.dp).background(Color.Black.copy(alpha = 0.05f), CircleShape))
                    }
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
