package com.example.note2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.note2.data.AppDatabase
import com.example.note2.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Thông báo mới"
        val body = message.notification?.body ?: message.data["body"] ?: "Bạn có một thông báo từ Note App"

        // 1. Hiển thị thông báo lên thanh trạng thái
        showNotification(title, body)

        // 2. Lưu vào Database Room
        saveNotificationToDatabase(title, body)
    }

    private fun saveNotificationToDatabase(title: String, body: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
        val database = AppDatabase.getDatabase(applicationContext)
        
        serviceScope.launch {
            val notification = NotificationModel(
                title = title,
                message = body,
                timestamp = System.currentTimeMillis(),
                userId = userId
            )
            database.notificationDao().insert(notification)
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "fcm_channel"
        val manager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo ứng dụng",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo cho các cập nhật của Note App"
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher) 
            .setAutoCancel(true) 
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
