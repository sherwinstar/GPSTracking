import 'dart:async';

import 'package:flutter/services.dart';
import 'package:rolling_beast/models/tracking_point.dart';
import 'package:rolling_beast/models/tracking.dart';
import 'package:uuid/uuid.dart';

class TrackingService {
  TrackingService._privateConstructor();
  static final TrackingService instance = TrackingService._privateConstructor();

  static const EventChannel _backgroundLocationStream = EventChannel('background_location_stream');

  StreamSubscription? _backgroundLocationSubscription;

  TrackingModel? currentTracking;

  Future<TrackingModel> startTracking(Function(TrackingPointModel) onNewTrackingPoint) async {
    _backgroundLocationSubscription?.cancel();


    currentTracking = TrackingModel(
      id: const Uuid().v4(),
      startTime: DateTime.now().millisecondsSinceEpoch,
    );

    _backgroundLocationSubscription = _backgroundLocationStream.receiveBroadcastStream().listen((dynamic event) async {
      print('new tracking point - $event');
      final TrackingPointModel newTrackingPoint = TrackingPointModel(
        id: const Uuid().v4(),
        trackingId: currentTracking!.id,
        latitude: event['latitude'],
        longitude: event['longitude'],
        speed: event['speed'],
        altitude: event['altitude'],
        horizontalAccuracy: event['horizontalAccuracy'],
        verticalAccuracy: event['verticalAccuracy'],
        course: event['course'],
        gpsTimestamp: event['gpsTimestamp'],
        relativeAltitude: event['relativeAltitude'],
        relativeAltitudeTimestamp: event['relativeAltitudeTimestamp'],
        timestamp: event['timestamp'],
      );
      
      currentTracking!.trackingPoints.add(newTrackingPoint);
      onNewTrackingPoint(newTrackingPoint);
    });

    return currentTracking!;
  }

  void stopTracking() {
    _backgroundLocationSubscription?.cancel();
    _backgroundLocationSubscription = null;
    currentTracking = null;
  }
}
