package com.example.SkyNote.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.SkyNote.MainActivity
import com.example.SkyNote.R
import com.example.SkyNote.data.local.AppDatabase
import com.example.SkyNote.data.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val noteId = intent.getStringExtra("noteId")
        val title = intent.getStringExtra("title") ?: "Ghi chú của bạn"
        val message = intent.getStringExtra("message") ?: "Nhấp để xem chi tiết"

        when (action) {
            ACTION_DAILY_REMINDER -> {
                val dailyTitle = "Xin Chào Buổi Sáng! ☀️"
                val dailyMsg = "Chúc bạn một buổi sáng vui vẻ, hãy quay lại ứng dụng để đọc lại những dòng note và viết những note mới nhé!"
                showNotification(context, 1001, dailyTitle, dailyMsg, null)
                saveToDatabase(context, dailyTitle, dailyMsg)
            }
            ACTION_NOTE_REMINDER -> {
                if (noteId != null) {
                    showNotification(context, noteId.hashCode(), title, message, noteId)
                    saveToDatabase(context, title, message)
                    updateNoteRemindedStatus(context, noteId)
                }
            }
        }
    }

    private fun showNotification(context: Context, notificationId: Int, title: String, message: String, noteId: String?) {
        val channelId = "note_reminders_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở ghi chú",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (noteId != null) {
                putExtra("noteId", noteId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun saveToDatabase(context: Context, title: String, message: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            db.notificationDao().insert(
                NotificationModel(
                    title = title,
                    message = message,
                    userId = userId,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun updateNoteRemindedStatus(context: Context, noteId: String) {
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val note = db.noteDao().getNoteById(noteId)
            if (note != null) {
                val updatedNote = note.copy(isReminded = true)
                db.noteDao().update(updatedNote)
                
                if (updatedNote.userId != "guest" && updatedNote.userId.isNotEmpty()) {
                    try {
                        FirebaseFirestore.getInstance()
                            .collection("users").document(updatedNote.userId)
                            .collection("notes").document(updatedNote.id)
                            .set(updatedNote)
                    } catch (e: Exception) {
                        Log.e("Reminder", "Error syncing reminded status: ${e.message}")
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_DAILY_REMINDER = "com.example.note2.ACTION_DAILY_REMINDER"
        const val ACTION_NOTE_REMINDER = "com.example.note2.ACTION_NOTE_REMINDER"

        fun scheduleNoteReminder(context: Context, noteId: String, title: String, description: String, timeInMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NoteNotificationReceiver::class.java).apply {
                action = ACTION_NOTE_REMINDER
                putExtra("noteId", noteId)
                putExtra("title", title)
                putExtra("message", description)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                noteId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
            Log.d("Reminder", "Scheduled reminder for note $noteId at $timeInMillis")
        }

        fun cancelNoteReminder(context: Context, noteId: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NoteNotificationReceiver::class.java).apply {
                action = ACTION_NOTE_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                noteId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
