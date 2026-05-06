package com.tareaapp.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tareaapp.utils.FirebaseUtils
import com.tareaapp.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        val title  = remoteMessage.notification?.title  ?: remoteMessage.data["title"]  ?: "TareaApp"
        val body   = remoteMessage.notification?.body   ?: remoteMessage.data["body"]   ?: ""
        val tareaId = remoteMessage.data["tareaId"] ?: ""

        NotificationHelper.showLocalNotification(
            context  = applicationContext,
            title    = title,
            body     = body,
            tareaId  = tareaId
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo FCM token: $token")

        val uid = FirebaseUtils.currentUid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseUtils.updateFcmToken(uid, token)
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando token: ${e.message}")
            }
        }
    }
}
