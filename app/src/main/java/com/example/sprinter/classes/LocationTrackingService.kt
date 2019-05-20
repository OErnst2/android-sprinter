package com.example.sprinter.classes

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sprinter.R
import com.example.sprinter.activities.StartActivity
import com.google.android.gms.maps.model.LatLng

class LocationTrackingService : Service() {

    var locationManager: LocationManager? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("a", name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)


            var builder = NotificationCompat.Builder(this, "a")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Sprinter")
                .setContentText("tracking")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()


            startForeground(10, builder)

        }
        if (locationManager == null)
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //--locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5f, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (i in 0..locationListeners.size) {
                try {
                    locationManager?.removeUpdates(locationListeners[i])
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
    }


    companion object {
        val TAG = "LocationTrackingService"

        val INTERVAL = 1000.toLong() // In milliseconds
        val DISTANCE = 1.toFloat() // In meters

        var currentPosition: LatLng? = null

        var shouldAppendArray = true

        val locationListeners = arrayOf(
            LTRLocationListener(LocationManager.GPS_PROVIDER)
            //LTRLocationListener(LocationManager.NETWORK_PROVIDER)
        )

        class LTRLocationListener(provider: String) : android.location.LocationListener {

            val lastLocation = Location(provider)

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                if (shouldAppendArray && lastLocation.accuracy < 5) {
                    Log.d(TAG, "CHANGE HAPPENED")
                    StartActivity.currentPoints.add(LatLng(lastLocation.latitude, lastLocation.longitude))
                }

                currentPosition = LatLng(lastLocation.latitude, lastLocation.longitude)
                Log.d("POSITION", lastLocation.latitude.toString() + " " + lastLocation.longitude.toString())
            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

        }
    }

}