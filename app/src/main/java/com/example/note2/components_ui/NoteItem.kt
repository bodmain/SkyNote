package com.example.note2.components_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.note2.model.NoteModel
import com.example.note2.ui.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable

fun NoteItem(
    note: NoteModel,
    onClick: (NoteModel) -> Unit,
    onDelete: (NoteModel) -> Unit
) {

    val randomBackgroundColor = remember(note.id) {
        NoteColors[note.id.hashCode().coerceAtLeast(0) % NoteColors.size]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick(note) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = randomBackgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Tiêu đề
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black.copy(alpha = 0.8f),
                    fontSize = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nội dung mô tả
            Text(
                text = note.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black.copy(alpha = 0.6f),
                    lineHeight = 20.sp
                ),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            //
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hiển thị ngày
                Text(
                    text = getRelativeTime(note.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Black.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Medium
                    )
                )

                // Nút xóa
                IconButton(
                    onClick = { onDelete(note) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Xóa",
                        tint = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    if (diff < 0) return "Vừa xong"

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)

    if (minutes < 1) {
        return "Vừa xong"
    }

    if (minutes < 60) {
        return "$minutes phút trước"
    }

    val noteCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCalendar = Calendar.getInstance().apply { timeInMillis = now }

    val isSameDay = noteCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
            noteCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR)

    return if (isSameDay) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } else {

        SimpleDateFormat("dd 'thg' M", Locale("vi", "VN")).format(Date(timestamp))
    }
}