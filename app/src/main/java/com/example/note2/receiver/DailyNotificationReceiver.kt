package com.example.note2.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.note2.MainActivity
import com.example.note2.R
import com.example.note2.data.AppDatabase
import com.example.note2.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = "Xin Chào Buổi Sáng! ☀️"
        val message = "Chúc bạn một buổi sáng vui vẻ, hãy quay lại ứng dụng để đọc lại những dòng note và viết những note mới nhé!"

        //  Hiển thị thông báo lên thanh trạng thái
        showNotification(context, title, message)

        //  Lưu vào Database
        saveToDatabase(context, title, message)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "daily_notification"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở hàng ngày",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
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

        notificationManager.notify(1002, notification)
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
}
