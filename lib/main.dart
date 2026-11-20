
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'native/canaux.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'TP4 Alertes',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
        useMaterial3: true,
      ),
      home: const AccueilPage(),
    );
  }
}

class AccueilPage extends StatefulWidget {
  const AccueilPage({super.key});

  @override
  State<AccueilPage> createState() => _AccueilPageState();
}

class _AccueilPageState extends State<AccueilPage>
    with WidgetsBindingObserver {

  String statut = 'Prêt';
  bool serviceActif = false;

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance.addObserver(this);

    Canaux.demanderPermission();

    Canaux.initialiserEcoute(
      surAction: () {
        if (mounted) {
          setState(() {
            statut = 'Action Voir reçue depuis la notification';
          });
        }
      },
    );

    verifierEtatService();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      verifierEtatService();
    }
  }

  Future<void> verifierEtatService() async {
    try {
      final actif = await Canaux.verifierService();

      if (mounted) {
        setState(() {
          serviceActif = actif;
          statut = actif ? 'Service actif' : 'Service arrêté';
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => statut = 'Erreur : $e');
      }
    }
  }

  Future<void> executer(
    Future<void> Function() action,
    String message,
  ) async {
    try {
      await action();

      if (mounted) {
        setState(() => statut = message);
      }
    } on PlatformException catch (e) {
      if (mounted) {
        setState(() => statut = 'Erreur : ${e.message}');
      }
    } catch (e) {
      if (mounted) {
        setState(() => statut = 'Erreur : $e');
      }
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('TP4 - Alertes natives'),
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [

              const Icon(
                Icons.notifications_active,
                size: 65,
              ),

              const SizedBox(height: 16),

              const Text(
                'Notifications et tâches en arrière-plan',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 19,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 24),

              ElevatedButton.icon(
                onPressed: () => executer(
                  () => Canaux.notifier(
                    titre: 'Alerte patient',
                    message: 'Une mesure critique a été détectée !',
                  ),
                  'Notification envoyée',
                ),
                icon: const Icon(Icons.notification_add),
                label: const Text('Tester notification'),
              ),

              const SizedBox(height: 12),

              ElevatedButton.icon(
                onPressed: () => executer(
                  Canaux.testerTravail,
                  'Tâche ponctuelle lancée',
                ),
                icon: const Icon(Icons.play_arrow),
                label: const Text('Tester tâche immédiate'),
              ),

              const SizedBox(height: 12),

              ElevatedButton.icon(
                onPressed: () => executer(
                  Canaux.planifierTravail,
                  'Tâche périodique planifiée (15 min)',
                ),
                icon: const Icon(Icons.schedule),
                label: const Text('Planifier tâche périodique'),
              ),

              const SizedBox(height: 12),

              OutlinedButton.icon(
                onPressed: () => executer(
                  Canaux.annulerTravail,
                  'Tâche périodique annulée',
                ),
                icon: const Icon(Icons.cancel),
                label: const Text('Annuler tâche périodique'),
              ),

              const SizedBox(height: 20),

              const Divider(),

              const Text(
                'Service de surveillance',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 12),

              ElevatedButton.icon(
                onPressed: () async {
                  await executer(
                    Canaux.demarrerService,
                    'Service de surveillance démarré',
                  );
                  await verifierEtatService();
                },
                icon: const Icon(Icons.monitor_heart),
                label: const Text('Démarrer surveillance'),
              ),

              const SizedBox(height: 12),

              OutlinedButton.icon(
                onPressed: () async {
                  await executer(
                    Canaux.arreterService,
                    'Service de surveillance arrêté',
                  );
                  await verifierEtatService();
                },
                icon: const Icon(Icons.stop),
                label: const Text('Arrêter surveillance'),
              ),

              const SizedBox(height: 12),

              ElevatedButton.icon(
                onPressed: verifierEtatService,
                icon: const Icon(Icons.check_circle),
                label: const Text('Vérifier état du service'),
              ),

              const SizedBox(height: 20),

              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        serviceActif
                            ? Icons.check_circle
                            : Icons.cancel,
                        color: serviceActif
                            ? Colors.green
                            : Colors.grey,
                      ),
                      const SizedBox(width: 10),
                      Text(
                        serviceActif
                            ? 'Surveillance active'
                            : 'Surveillance inactive',
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 12),

              Text(
                statut,
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
