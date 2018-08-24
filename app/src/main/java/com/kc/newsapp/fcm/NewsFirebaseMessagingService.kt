package com.kc.newsapp.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kc.newsapp.R
import com.kc.newsapp.ui.ArticleListActivity
import com.kc.newsapp.util.Util

class NewsFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        remoteMessage?.let {
            Util.log("From: ${it.from}, Id: ${it.messageId}, Type: ${it.messageType}, To: ${it.to}")
            if (it.data.isNotEmpty()) {
                Util.log("Message data payload: ${it.data}")

            }

            it.notification?.let {
                Util.log("Message Notification Body: ${it.body}")
                sendNotification(remoteMessage.data["title"], it.body!!)
            }
        }
    }

    override fun onNewToken(token: String?) {
        Util.log("onNewToken $token")
        sendRegistrationToServer(token)
    }

    fun sendRegistrationToServer(token: String?) {

    }

    private fun sendNotification(title: String? = "FCM Message", messageBody: String) {
        val intent = Intent(this, ArticleListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_notification_news)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            notificationBuilder.setSmallIcon(R.drawable.icon_transperent)
//            notificationBuilder.color = resources.getColor(android.R.color.white)
//        } else {
//            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
//        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }


}