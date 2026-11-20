
import 'package:flutter/services.dart';

class Canaux {
  static const MethodChannel _channel =
      MethodChannel('tn.isetbizerte.tp4_alertes/alertes');

  static Future<void> demanderPermission() async {
    await _channel.invokeMethod('demanderPermission');
  }

  static Future<void> notifier({
    required String titre,
    required String message,
  }) async {
    await _channel.invokeMethod('notifier', {
      'titre': titre,
      'message': message,
    });
  }

  static Future<void> testerTravail() async {
    await _channel.invokeMethod('testerTravail');
  }

  static Future<void> planifierTravail() async {
    await _channel.invokeMethod('planifierTravail');
  }

  static Future<void> annulerTravail() async {
    await _channel.invokeMethod('annulerTravail');
  }

  static Future<void> demarrerService() async {
    await _channel.invokeMethod('demarrerService');
  }

  static Future<void> arreterService() async {
    await _channel.invokeMethod('arreterService');
  }

  static Future<bool> verifierService() async {
    return await _channel.invokeMethod<bool>('verifierService') ?? false;
  }

  static void initialiserEcoute({
    required void Function() surAction,
  }) {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'actionInitiale' ||
          call.method == 'surAction') {
        surAction();
      }
    });
  }
}
