package com.bashar.mawaqit

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AdhanService : Service() {
    private var ringtone: android.media.Ringtone? = null
    override fun onCreate() {
        super.onCreate()
        val channelId = "adhan_channel"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "تنبيهات الصلاة", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان وقت الصلاة")
            .setContentText("حي على الصلاة")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(false)
            .build()
        startForeground(7, notification)
        try {
            ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            ringtone?.play()
        } catch (_: Exception) { }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            try { Thread.sleep(60_000) } catch (_: InterruptedException) {}
            stopSelf()
        }.start()
        return START_NOT_STICKY
    }
    override fun onDestroy() {
        try { ringtone?.stop() } catch (_: Exception) {}
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
