
package tn.isetbizerte.tp4_alertes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val channelName = "tn.isetbizerte.tp4_alertes/alertes"
    private var methodChannel: MethodChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        Alertes.creerCanal(this)

        methodChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            channelName
        )

        methodChannel?.setMethodCallHandler { call, result ->

            when (call.method) {

                "notifier" -> {
                    val titre = call.argument<String>("titre") ?: "Alerte"
                    val message = call.argument<String>("message") ?: ""

                    Alertes.envoyer(this, titre, message)
                    result.success(true)
                }

                "demanderPermission" -> {
                    demanderPermission()
                    result.success(true)
                }

                "testerTravail" -> {
                    TravailFond.tester(this)
                    result.success(true)
                }

                "planifierTravail" -> {
                    TravailFond.planifier(this)
                    result.success(true)
                }

                "annulerTravail" -> {
                    TravailFond.annuler(this)
                    result.success(true)
                }

                "demarrerService" -> {
                    ContextCompat.startForegroundService(
                        this,
                        Intent(this, ServiceSurveillance::class.java)
                    )
                    result.success(true)
                }

                "arreterService" -> {
                    stopService(
                        Intent(this, ServiceSurveillance::class.java)
                    )
                    result.success(true)
                }

                "verifierService" -> {
                    result.success(ServiceSurveillance.estActif())
                }

                else -> result.notImplemented()
            }
        }

        traiterActionInitiale()
    }

    private fun traiterActionInitiale() {
        if (intent?.action == "VOIR_ALERTE") {
            methodChannel?.invokeMethod("actionInitiale", null)
            intent?.action = null
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.action == "VOIR_ALERTE") {
            methodChannel?.invokeMethod("surAction", null)
            intent.action = null
        }
    }

    private fun demanderPermission() {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                2001
            )
        }
    }
}
