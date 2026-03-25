package com.example.note2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        // Lấy dữ liệu từ cả notification body và data payload
        val title = message.notification?.title ?: message.data["title"] ?: "Thông báo mới"
        val body = message.notification?.body ?: message.data["body"] ?: "Bạn có một thông báo từ Note App"

        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "fcm_channel"
        val manager = getSystemService(NotificationManager::class.java)

        // 1. Tạo Notification Channel cho Android O trở lên
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

        // 2. Tạo Intent để mở app khi click vào thông báo
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Xây dựng thông báo
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher) // Sử dụng icon của app
            .setAutoCancel(true) // Tự biến mất khi click
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Gửi token này lên server của bạn nếu cần để gửi thông báo định danh
    }
}
