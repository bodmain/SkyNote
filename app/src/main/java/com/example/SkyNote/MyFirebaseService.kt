package com.example.SkyNote

import com.example.SkyNote.data.local.AppDatabase
import com.example.SkyNote.data.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title ?: "SkyNote"
        val body = message.notification?.body ?: ""
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.notificationDao().insert(
                NotificationModel(
                    title = title,
                    message = body,
                    userId = userId
                )
            )
        }
    }
}
