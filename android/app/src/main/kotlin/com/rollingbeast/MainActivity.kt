package com.rollingbeast

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.app.*
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

import com.huawei.hms.location.*

class MainActivity: FlutterActivity(), EventChannel.StreamHandler, SensorEventListener {
  private var locationClient: FusedLocationProviderClient? = null

  private var backgroundLocationStream: EventChannel? = null

  var trackingOptions: TrackingOptions = TrackingOptions(0.0)

  private var locationResult: MethodChannel.Result? = null

  private var lastLocationData: LocationData? = null
  private var lastRelativeAltitudeData: RelativeAltitudeData? = null
  private var initialAltitude: Float? = null

  private var sensorManager: SensorManager? = null
  private var backgroundLocationCallback: LocationCallback? = null

  override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
    super.configureFlutterEngine(flutterEngine)

    backgroundLocationStream = EventChannel(
      flutterEngine.dartExecutor.binaryMessenger,
      "background_location_stream"
    )

    backgroundLocationStream!!.setStreamHandler(this)

    MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "location_channel")
      .setMethodCallHandler { call, result ->
        when (call.method) {
          "getCurrentLocation" -> {
            requestLocationUpdates(isSingle = true) { locationData ->
              result.success(locationData.toMap())
            }
          }

          "setTrackingOptions" -> {
            val args = call.arguments as Map<*, *>
            trackingOptions = TrackingOptions(args["distanceFilter"] as Double)
          }

          else -> result.notImplemented()
        }
      }
  }

  private fun requestLocationUpdates(isSingle: Boolean, onReceivedLocation: (LocationData) -> Unit): LocationCallback {
    if (locationClient == null) {
      Log.d("kotlin","location client is null")
      locationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    Log.d("kotlin","location client is not null $locationClient")

//    huawei
    val locationRequest = LocationRequest()
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    locationRequest.interval = 1000
    if (isSingle) locationRequest.numUpdates = 1
//    locationRequest.fastestInterval = 500
//    locationRequest.maxWaitTime = 2000


    Log.d("kotlin", "location request $locationRequest")

    val locationCallback: LocationCallback by lazy {
      object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
          if (locationResult == null) return;
          onReceivedLocation(LocationData.fromLocation(locationResult))
        }
      }
    }

    locationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
      ?.addOnSuccessListener {
        Log.d("kotlin", "successfully got location - $locationCallback")
      }
      ?.addOnFailureListener {
        Log.d("kotlin", "failed to get location")
      }

    return locationCallback
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    // huawei stop background location if already running
    locationClient?.disableBackgroundLocation()
    locationClient?.removeLocationUpdates(backgroundLocationCallback)
    sensorManager?.unregisterListener(this)
    locationClient?.enableBackgroundLocation(1, buildNotification())

    startRelativeAltitudeTracking()
    //set isSingle true and restart it to avoid the bug in huawei continue location.
    backgroundLocationCallback = requestLocationUpdates(isSingle = false) { locationData ->
      Log.d("kotlin","Yoyoyo got location data: $locationData")
      val trackingData = TrackingData(
        locationData,
        lastRelativeAltitudeData ?: RelativeAltitudeData.fromRelativeAltitude(0.0),
        source = TrackingDataSource.GPS,
      )
      events?.success(trackingData.toMap())
    }
  }

  override fun onCancel(arguments: Any?) {
    // huawei
    locationClient?.disableBackgroundLocation()
    locationClient?.removeLocationUpdates(backgroundLocationCallback)

    sensorManager?.unregisterListener(this)
  }

  private fun buildNotification(): Notification {
    val content = "in progress... Latitude: ${lastLocationData?.latitude ?: 0}. Longitude: ${lastLocationData?.longitude ?: 0}. Relative Altitude: ${lastRelativeAltitudeData?.relativeAltitude ?: -1}m. Absolute Altitude: ${lastLocationData?.altitude ?: -1}m"

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = context.packageName
    val notificationChannel = NotificationChannel(channelId, "LOCATION", NotificationManager.IMPORTANCE_LOW)
    notificationManager.createNotificationChannel(notificationChannel)

    return NotificationCompat.Builder(context, channelId)
      .setAutoCancel(false)
      .setOngoing(true)
      .setContentTitle("Tracking")
      .setStyle(NotificationCompat.BigTextStyle().bigText(content))
      .setContentText(content)
      .build()
  }

  private fun requestPermission() {
    // Android SDK<=28 所需权限动态申请
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
      if (ActivityCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        val strings = arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(this, strings, 1)
      }
    } else {
      // Android SDK > 28 所需权限动态申请，需添加“android.permission.ACCESS_BACKGROUND_LOCATION”权限。
      if (ActivityCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
          this,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
          this,
          "android.permission.ACCESS_BACKGROUND_LOCATION"
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        val strings = arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION,
          "android.permission.ACCESS_BACKGROUND_LOCATION"
        )
        ActivityCompat.requestPermissions(this, strings, 2)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestPermission()
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event != null && lastLocationData != null) {
      val altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, event.values[0])
      if (initialAltitude == null) initialAltitude = altitude
      val relativeAltitude = (altitude - initialAltitude!!).toDouble()
      val relativeAltitudeData = RelativeAltitudeData.fromRelativeAltitude(relativeAltitude = relativeAltitude)
      lastRelativeAltitudeData = relativeAltitudeData
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

  private fun startRelativeAltitudeTracking() {
    Log.d("kotlin","Start altitude tracking")
    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val barometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE)
    sensorManager!!.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL)
  }
}