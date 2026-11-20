
package tn.isetbizerte.tp4_alertes

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class VerificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            delay(2000)

            // Simulation d'un niveau de batterie faible
            val batterie = inputData.getInt("batterie", 15)

            if (batterie < 20) {
                Alertes.envoyer(
                    applicationContext,
                    "Batterie faible",
                    "Le niveau de batterie est de $batterie %"
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
