import 'package:rolling_beast/models/models.dart';

class TrackingGroupingOptions {
  double uphillSamplesSize;
  double uphillChangeDiff;

  double downhillSamplesSize;
  double downhillChangeDiff;

  double stillSamplesSize;
  double stillDistanceFilter;

  TrackingGroupingOptions({
    required this.uphillSamplesSize,
    required this.uphillChangeDiff,
    required this.downhillSamplesSize,
    required this.downhillChangeDiff,
    required this.stillSamplesSize,
    required this.stillDistanceFilter,
  });
}

class TrackingModel {
  String id;
  int startTime;
  List<TrackingPointModel> trackingPoints = [];

  TrackingModel({required this.id, required this.startTime, List<TrackingPointModel>? trackingPoints}) : trackingPoints = trackingPoints ?? [];
}
