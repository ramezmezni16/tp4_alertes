
package tn.isetbizerte.tp4_alertes

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.*

class ServiceSurveillance : Service() {

    companion object {
        const val CHANNEL_ID = "surveillance_continue"
        const val NOTIFICATION_ID = 2001
        const val ACTION_ARRETER = "ARRETER_SURVEILLANCE"

        @Volatile
        var actif = false
            private set

        fun estActif(): Boolean = actif
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var surveillanceJob: Job? = null
    private var enAlerte = false
    private var temperature = 37.0

    override fun onCreate() {
        super.onCreate()
        creerCanal()
        actif = true
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action == ACTION_ARRETER) {
            actif = false
            scope.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        demarrerNotification()

        if (surveillanceJob?.isActive != true) {
            surveillanceJob = scope.launch {
                while (isActive) {
                    temperature += 0.2

                    if (temperature >= 39.0 && !enAlerte) {
                        enAlerte = true

                        Alertes.envoyer(
                            applicationContext,
                            "Température critique",
                            "Température détectée : %.1f °C".format(temperature)
                        )
                    } else if (temperature < 38.0) {
                        enAlerte = false
                    }

                    mettreAJourNotification()
                    delay(10000)
                }
            }
        }

        return START_STICKY
    }

    private fun creerCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Surveillance continue",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun demarrerNotification() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            construireNotification(),
            if (Build.VERSION.SDK_INT >= 34) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )
    }

    private fun construireNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            3001,
            Intent(this, ServiceSurveillance::class.java).apply {
                action = ACTION_ARRETER
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Surveillance active")
            .setContentText(
                "Température : %.1f °C".format(temperature)
            )
            .setOngoing(true)
            .addAction(0, "Arrêter", stopIntent)
            .build()
    }

    private fun mettreAJourNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, construireNotification())
    }

    override fun onDestroy() {
        actif = false
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
