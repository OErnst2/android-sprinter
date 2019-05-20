package com.example.sprinter.activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sprinter.R
import com.example.sprinter.activities.MainActivity.Companion.units
import com.example.sprinter.classes.LocationTrackingService
import com.example.sprinter.classes.StopWatch
import com.example.sprinter.databases.StaticData
import com.example.sprinter.fragments.SettingsFragment
import com.example.sprinter.models.Statistic
import com.example.sprinter.models.Track
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.start_fragment.*
import java.util.*
import kotlin.collections.ArrayList

class StartActivity : AppCompatActivity(), OnMapReadyCallback {

    private var going = false
    private var paused = false

    private lateinit var mMap: GoogleMap
    private var autoCamera = true

    private var track: Track? = null

    private var isUpdatingMapGoing = true

    private var trackList = listOf(LatLng(10.0, 11.0), LatLng(11.0, 9.0), LatLng(12.0, 15.0), LatLng(20.0, 20.0))

    private var stopWatch = StopWatch(this)

    private var serviceIntent: Intent? = null

    private var lastListSize = 0
    private var currentDistance = 0.0
    private var lastPoint: LatLng? = null
    private var lastTimeSeconds: Int? = null

    private var isIndianRunEnabled = false
    private var indianRunTime = 0
    private var shouldRun = true
    private var indianWalkTime = 0
    private var timeToNotification = 0


    companion object {
        var currentPoints = ArrayList<LatLng>()
        var totalPoints = ArrayList<ArrayList<LatLng>>()
        const val KM_TO_MILES = 0.621371192
        var staticTrack = Track()
    }


    fun start() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            Intent(this, LocationTrackingService::class.java).also { intent ->
                startForegroundService(intent)
                serviceIntent = intent
            }
        } else {
            Intent(this, LocationTrackingService::class.java).also { intent ->
                startService(intent)
                serviceIntent = intent
            }
        }
        autoCamera = true
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        staticTrack = Track()
        setContentView(R.layout.start_fragment)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        isIndianRunEnabled = sharedPreferences.getBoolean(SettingsFragment.keyEnableIndianPreference, false)
        indianRunTime = sharedPreferences.getInt(SettingsFragment.keyRunTimeIndianPreference, 1)
        indianWalkTime = sharedPreferences.getInt(SettingsFragment.keyWalkTimeIndianPreference, 1)

        findViewById<TextView>(R.id.distanceTextView).text = "0.00 $units"
        findViewById<TextView>(R.id.averageSpeedView).text = "0.000 $units/h"
        findViewById<TextView>(R.id.actualSpeed).text = "0.000 $units/h"

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        start_button.setOnClickListener {
            buttons.visibility = View.VISIBLE
            start_button.visibility = View.GONE
            if (!going) {
                start()
                going = true
            }
            stopWatch.startstopWatch()
            LocationTrackingService.shouldAppendArray = true
            paused = false
        }


        val index = intent.getIntExtra("index", -1)
        if (index != -1) {
            track = StaticData.tracks[index]
        }



        pause_button.setOnClickListener {
            buttons.visibility = View.GONE
            start_button.visibility = View.VISIBLE
            start_button.text = getString(R.string.continue_)
            totalPoints.add(currentPoints)
            stopWatch.pauseClock()
            lastListSize = 0
            lastPoint = null
            lastTimeSeconds = null
            LocationTrackingService.shouldAppendArray = false
            currentPoints = ArrayList()
            paused = true
        }

        stop_button.setOnClickListener {


            isUpdatingMapGoing = false

            if (serviceIntent != null) {
                stopService(serviceIntent)
            }
            totalPoints.add(currentPoints)
            currentPoints = ArrayList()

            StaticData.updateStatistics(
                Statistic(
                    track?.name ?: "Custom",
                    currentDistance / 1000,
                    stopWatch.minutes * 60 + stopWatch.seconds,
                    Date(1999, 10, 12),
                    "hard"
                )
            )
            stopWatch.endClock()
            LocationTrackingService.currentPosition = null
            currentDistance = 0.0
            lastPoint = null
            lastListSize = 0
            lastTimeSeconds = null
            totalPoints = ArrayList()
            going = false
            paused = false

            //|| true for testing
            if ((totalPoints.size == 1 && track == null && totalPoints[0].size > 10) || true) {
                dialogForSave()
            }
        }
    }

    private fun goToSaveActivity() {
        val mockArray = arrayListOf<LatLng>(LatLng(5.5, 9.8), LatLng(8.8, 20.0))
        staticTrack = Track(currentDistance / 1000, mockArray)//totalPoints[0])
        startActivity(Intent(this, CreateTrackActivity::class.java))
        finish()
    }


    @SuppressLint("SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLoadedCallback {
            Thread {
                var wasSixSec = 3
                while (isUpdatingMapGoing) {
                    runOnUiThread {
                        Log.d("CONTROL", "$totalPoints $currentPoints")
                        updateMap()

                        if (lastListSize < currentPoints.size) {
                            if (lastPoint == null) {
                                lastPoint = currentPoints[0]
                            }

                            val distanceBetweenLastPoints = distanceCounting()
                            averageSpeedCounting()
                            currentSpeedCount(distanceBetweenLastPoints)

                            lastTimeSeconds = stopWatch.minutes * 60 + stopWatch.seconds
                            wasSixSec = 3

                        } else {
                            wasSixSec--
                            if (wasSixSec == 0) {
                                findViewById<TextView>(R.id.actualSpeed).text = "0.000 $units/h"
                                wasSixSec = 3
                            }
                        }
                        Log.d("DISTANCE", "$currentDistance $lastTimeSeconds")
                    }
                    Log.d("CheckNotificationTime", "$going, $isIndianRunEnabled, $paused , $timeToNotification")
                    if (going && isIndianRunEnabled && !paused && timeToNotification == 0) {
                        makeNotification()

                        timeToNotification = if (shouldRun) indianRunTime * 60 else indianWalkTime * 60
                        shouldRun = !shouldRun
                    }
                    Thread.sleep(2000)
                    if (going && isIndianRunEnabled && !paused && timeToNotification != 0) {
                        timeToNotification -= 2
                    }
                }
            }.start()
            zoomOnTrack()
        }


        mMap.setOnCameraMoveListener {
            autoCamera = false
        }

        locationButton.setOnClickListener {
            autoCamera = true
            Log.d("currentPos", currentPoints.toString())
            Log.d("totalPoints", totalPoints.toString())
            if (LocationTrackingService.currentPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationTrackingService.currentPosition!!, 18f))
            }
        }

    }

    private fun zoomOnTrack() {
        if (track != null && track!!.points.size > 0) {
            val boundBuilder = LatLngBounds.builder()
            for (point: LatLng in track!!.points) {
                boundBuilder.include(point)
            }
            val bounds = boundBuilder.build()
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
        }
    }

    private fun updateMap() {
        mMap.clear()

        for (line in totalPoints) {
            mMap.addPolyline(PolylineOptions().addAll(line).color(Color.RED))
        }

        mMap.addPolyline(PolylineOptions().addAll(currentPoints).color(Color.RED))

        if (track != null && track!!.points.size > 0) {
            mMap.addPolyline(PolylineOptions().addAll(track!!.points).color(R.color.abc_primary_text_material_dark))
        }

        if (LocationTrackingService.currentPosition != null) {
            mMap.addMarker(
                MarkerOptions().position(LocationTrackingService.currentPosition!!).flat(false)
            )

            if (autoCamera) {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(LocationTrackingService.currentPosition!!, 18f)
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun averageSpeedCounting() {
        val currentHours: Double = (stopWatch.minutes * 60 + stopWatch.seconds).toDouble() / 3600
        var averageSpeed: Double = currentDistance / 1000 / currentHours

        if (averageSpeed.toString() != Double.NaN.toString()) {
            if (units == "Km") {
                findViewById<TextView>(R.id.averageSpeedView).text = "${String.format("%.3f", averageSpeed)} Km/h"

            } else {
                averageSpeed = currentDistance / 1000 * KM_TO_MILES / currentHours
                findViewById<TextView>(R.id.averageSpeedView).text = "${String.format("%.3f", averageSpeed)} Mil/h"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun distanceCounting(): Double {
        val twoLastPointsDistance =
            SphericalUtil.computeDistanceBetween(lastPoint, currentPoints[currentPoints.size - 1])
        currentDistance += twoLastPointsDistance

        lastPoint = currentPoints[currentPoints.size - 1]
        lastListSize = currentPoints.size
        if (units == "Mil") {
            findViewById<TextView>(R.id.distanceTextView).text =
                "${String.format("%.2f", currentDistance / 1000 * KM_TO_MILES)} $units"
        } else {
            findViewById<TextView>(R.id.distanceTextView).text =
                "${String.format("%.2f", currentDistance / 1000)} $units"
        }
        return twoLastPointsDistance
    }

    @SuppressLint("SetTextI18n")
    private fun currentSpeedCount(distance: Double) {
        if (lastTimeSeconds != null) {
            val hours: Double =
                ((stopWatch.minutes * 60 + stopWatch.seconds - lastTimeSeconds!!).toDouble() / 3600)

            if (units == "Km") {
                val actualSpeed = distance / 1000 / hours
                findViewById<TextView>(R.id.actualSpeed).text = "${String.format("%.3f", actualSpeed)} Km/h"

            } else {
                val actualSpeed = distance / 1000 * KM_TO_MILES / hours
                findViewById<TextView>(R.id.actualSpeed).text = "${String.format("%.3f", actualSpeed)} Mil/h"
            }
        }
        lastTimeSeconds = stopWatch.minutes * 60 + stopWatch.seconds

    }

    override fun onBackPressed() {
        if (going) {
            AlertDialog.Builder(this)
                .setMessage(R.string.leave_start_text)
                .setCancelable(false)
                .setPositiveButton(
                    R.string.yes
                ) { dialog, id ->
                    run { ->
                        if (serviceIntent != null) {
                            stopService(serviceIntent)
                        }
                        stopWatch.endClock()
                        isUpdatingMapGoing = false
                        super.onBackPressed()
                    }
                }
                .setNegativeButton(R.string.no)
                { dialog, id -> }
                .show()
        } else {
            if (serviceIntent != null) {
                stopService(serviceIntent)
            }
            stopWatch.endClock()
            isUpdatingMapGoing = false
            super.onBackPressed()
        }
    }


    private fun dialogForSave() {

        AlertDialog.Builder(this)
            .setMessage(getString(R.string.would_you_like_to_save_on_server))
            .setCancelable(false)
            .setPositiveButton(
                R.string.yes
            ) { dialog, id -> goToSaveActivity() }
            .setNegativeButton(R.string.no)
            { dialog, id -> finish() }
            .show()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    private val CHANNEL_ID = "IndianRunChannel"
    private val NOTIFICATION_ID = 42

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) != null) {
                return
            }
            val name = "IndianRunChannel"
            val description = "Indian"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            manager.createNotificationChannel(channel)
        }
    }

    private fun makeNotification() {
        createNotificationChannel()

        val notifyIntent = Intent(this, SettingsActivity::class.java).putExtra("foo", "bar")
        notifyIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val runOrNot = if (shouldRun) getString(R.string.you_should_run) else getString(R.string.you_should_walk)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.lake)
            .setContentTitle(getString(R.string.sprinter_indian_run))
            .setContentText(runOrNot)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(false).setSmallIcon(R.mipmap.ic_launcher)

        val manager = NotificationManagerCompat.from(this)
        manager.notify(NOTIFICATION_ID, builder.build())
    }
}