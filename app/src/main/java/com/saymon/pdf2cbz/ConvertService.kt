package com.saymon.pdf2cbz

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Фоновый сервис: держит процесс живым, пока идёт конвертация,
 * и показывает прогресс в шторке. Отмена из шторки — через общий флаг.
 */
class ConvertService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIF_ID, buildNotif("Готовимся…", 0, 0))
            }
            ACTION_UPDATE -> {
                val done = intent.getIntExtra(EXTRA_DONE, 0)
                val total = intent.getIntExtra(EXTRA_TOTAL, 0)
                val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
                nm().notify(NOTIF_ID, buildNotif(text, done, total))
            }
            ACTION_CANCEL -> {
                ConvertControl.cancelled.set(true)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun nm() = getSystemService(NotificationManager::class.java)

    private fun channel() {
        val ch = NotificationChannel(
            CHANNEL, "Конвертация", NotificationManager.IMPORTANCE_LOW)
        nm().createNotificationChannel(ch)
    }

    private fun cancelIntent(): PendingIntent {
        val i = Intent(this, ConvertService::class.java).setAction(ACTION_CANCEL)
        return PendingIntent.getService(this, 1, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun buildNotif(text: String, done: Int, total: Int): Notification {
        channel()
        val open = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("PDF2CBZ: конвертация")
            .setContentText(text)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total, done, total <= 0)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                "Отмена", cancelIntent())
            .build()
    }

    companion object {
        const val ACTION_START = "com.saymon.pdf2cbz.svc.START"
        const val ACTION_UPDATE = "com.saymon.pdf2cbz.svc.UPDATE"
        const val ACTION_CANCEL = "com.saymon.pdf2cbz.svc.CANCEL"
        const val ACTION_STOP = "com.saymon.pdf2cbz.svc.STOP"
        const val EXTRA_DONE = "done"
        const val EXTRA_TOTAL = "total"
        const val EXTRA_TEXT = "text"
        const val CHANNEL = "convert"
        const val NOTIF_ID = 41

        fun cmd(ctx: Context, action: String,
                done: Int = 0, total: Int = 0, text: String = "") {
            val i = Intent(ctx, ConvertService::class.java)
                .setAction(action)
                .putExtra(EXTRA_DONE, done)
                .putExtra(EXTRA_TOTAL, total)
                .putExtra(EXTRA_TEXT, text)
            ctx.startForegroundService(i)
        }
    }
}

/** Общий флаг отмены: кнопка в приложении и в шторке жмут одно. */
object ConvertControl {
    val cancelled = AtomicBoolean(false)
}
