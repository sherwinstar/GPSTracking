import 'package:intl/intl.dart';
import 'package:latlong2/latlong.dart';

enum TrackingPointType { uphill, downhill, still, undetermined }

class TrackingPointModel {
  final String id;
  final String trackingId;
  final double latitude;
  final double longitude;
  final double speed;
  final double? altitude;
  final double? horizontalAccuracy;
  final double? verticalAccuracy;
  final double? course;
  final int gpsTimestamp;
  final double relativeAltitude;
  final int relativeAltitudeTimestamp;
  final int timestamp;

  bool showOnChart = false;

  LatLng get coords => LatLng(latitude, longitude);

  TrackingPointModel({
    required this.id,
    required this.trackingId,
    required this.latitude,
    required this.longitude,
    required this.speed,
    required this.altitude,
    required this.horizontalAccuracy,
    required this.verticalAccuracy,
    required this.course,
    required this.gpsTimestamp,
    required this.relativeAltitude,
    required this.relativeAltitudeTimestamp,
    required this.timestamp,
  });

  double distanceTo(LatLng point) => const Distance()(coords, point);

  String get formattedTimestamp {
    final dt = DateTime.fromMillisecondsSinceEpoch(gpsTimestamp);
    return DateFormat('yyyy-dd-MM, hh:mm:ss').format(dt);
  }


  static TrackingPointModel fromJson(Map<String, dynamic> json) {
    return TrackingPointModel(
      id: json['id'],
      trackingId: json['trackingId'],
      latitude: json['latitude'],
      longitude: json['longitude'],
      speed: json['speed'],
      altitude: json['altitude'],
      horizontalAccuracy: json['horizontalAccuracy'],
      verticalAccuracy: json['verticalAccuracy'],
      course: json['course'],
      gpsTimestamp: json['gpsTimestamp'],
      relativeAltitude: json['relativeAltitude'],
      relativeAltitudeTimestamp: json['relativeAltitudeTimestamp'],
      timestamp: json['timestamp'],
    );
  }
}
