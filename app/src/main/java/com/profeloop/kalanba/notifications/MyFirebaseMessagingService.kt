package com.profeloop.kalanba.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.profeloop.kalanba.utils.FirebaseUtils
import com.profeloop.kalanba.utils.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "ProfeLoop"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        NotificationHelper.show(this, title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseUtils.currentUid ?: return
        FirebaseUtils.db.collection("users").document(uid)
            .update("fcmToken", token)
    }
}
