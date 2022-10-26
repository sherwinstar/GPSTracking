import 'package:flutter/material.dart';
import 'package:rolling_beast/services/tracking_service.dart';

import 'models/models.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      debugShowCheckedModeBanner: false,
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  bool isTracking = false;
  List<TrackingPointModel> gpsPoints = [];

  void startTracking() {
    setState(() => isTracking = true);
    TrackingService.instance.startTracking((gpsPoint) {
      setState(() => gpsPoints.add(gpsPoint));
    });
  }

  void stopTracking() {
    setState(() => isTracking = false);
    TrackingService.instance.stopTracking();
  }

  Widget listItem({required TrackingPointModel point, required int index, bool? isLast = false}) {
    return Container(
      width: double.maxFinite,
      padding: EdgeInsets.all(8),
      decoration: BoxDecoration(
          border: Border(
        bottom: BorderSide(width: 0.5, color: Colors.black26),
      )),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text('#${index + 1} - Time: ${point.formattedTimestamp}', style: TextStyle(color: Colors.grey.shade500, fontSize: 12),),
          Container(height: 5),
          Text('LatLong: ${point.longitude.toString()} - ${point.latitude.toString()}', style: TextStyle(fontSize: 13)),
          Container(height: 5),
          Text('Speed: ${point.speed.toString()}, Altitude: ${point.altitude.toString()}', style: TextStyle(fontSize: 13)),
          Container(height: 5),
          Text('Horizontal Acc: ${point.horizontalAccuracy?.toStringAsFixed(2)}, Vertical Acc: ${point.verticalAccuracy?.toStringAsFixed(2)}', style: TextStyle(fontSize: 13)),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('GPS Tracking'),
        actions: [
          if (gpsPoints.isNotEmpty) GestureDetector(
            onTap: () => setState(() => gpsPoints.clear()),
            child: Center(
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: Text('Clear'),
              ),
            ),
          )
        ],
      ),
      body: gpsPoints.isEmpty ? Center(child: Text('Press Start')) : ListView.builder(
        itemCount: gpsPoints.length,
        itemBuilder: (BuildContext context, int index) {
          return listItem(index: index, isLast: index == gpsPoints.length, point: gpsPoints[index]);
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: isTracking ? stopTracking : startTracking,
        tooltip: 'Increment',
        child: Icon(isTracking ? Icons.pause : Icons.play_arrow),
      ),
    );
  }
}
