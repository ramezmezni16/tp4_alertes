
package tn.isetbizerte.tp4_alertes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlerteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "IGNORER") {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as android.app.NotificationManager
            manager.cancel(Alertes.NOTIFICATION_ID)
        }
    }
}
