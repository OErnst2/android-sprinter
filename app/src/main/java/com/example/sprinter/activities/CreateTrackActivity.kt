package com.example.sprinter.activities


import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sprinter.R
import com.example.sprinter.classes.getUrlString
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_create_track.*




class CreateTrackActivity : AppCompatActivity(), OnMapReadyCallback {
    var googleMap: GoogleMap? = null
    override fun onMapReady(mMap: GoogleMap?) {
        googleMap = mMap

        googleMap?.setOnMapLoadedCallback {
            mMap?.addPolyline(PolylineOptions().addAll(StartActivity.staticTrack.points).color(Color.RED))
            zoomOnTrack()
        }
    }

    private fun zoomOnTrack() {
        if (StartActivity.staticTrack.points.size > 0) {
            val boundBuilder = LatLngBounds.builder()
            for (point: LatLng in StartActivity.staticTrack.points) {
                boundBuilder.include(point)
            }
            val bounds = boundBuilder.build()
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
        }
    }

    private var type: String = "city"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_track)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        send_button.setOnClickListener {
            val rawName = name.text.toString()
            if (rawName.length > 15 || rawName.getUrlString() == "failed ") {
                Toast.makeText(this, getString(R.string.invalid_name), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val trackName = rawName.trim().getUrlString()
            val difficulty = if (radioButton.isChecked) "easy" else if (radioButton2.isChecked) "medium" else "hard"

            var points = ""
            for (point in StartActivity.staticTrack.points) {
                points += point.latitude.toString() + ":" + point.longitude.toString() + ";"
            }
            points = points.trim(';')
            val length = StartActivity.staticTrack.length.toString()
            Ion.with(this)
                .load("https://sprinter-server.000webhostapp.com/add_track.php?name=$trackName&difficulty=$difficulty&points=$points&type=$type&length=$length")
                .asString(Charsets.UTF_8)
                .setCallback { e, result ->
                    Toast.makeText(
                        this,
                        when (result) {
                            "1" -> getString(R.string.sent)
                            "2" -> getString(R.string.name_already_used)
                            else -> getString(
                                R.string.there_was_an_error_on_the_server)
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                    println(result)

                    if (result == "1") {
                        Handler().postDelayed({
                            finish()
                        }, 1000)
                    }
                }
        }
        val resourceId = resources?.getIdentifier(
            "button_background", "drawable",
            packageName
        )
        city_button.setOnClickListener {
            type = "city"
            resourceId ?: return@setOnClickListener
            it.background = resources.getDrawable(resourceId)
            changeChoice(0)
        }
        forest_button.setOnClickListener {
            type = "forest"
            resourceId ?: return@setOnClickListener
            it.background = resources.getDrawable(resourceId)
            changeChoice(1)
        }
        field_button.setOnClickListener {
            type = "field"
            resourceId ?: return@setOnClickListener
            it.background = resources.getDrawable(resourceId)
            changeChoice(2)
        }
        lake_button.setOnClickListener {
            type = "lake"
            resourceId ?: return@setOnClickListener
            it.background = resources.getDrawable(resourceId)
            changeChoice(3)
        }
        mountain_button.setOnClickListener {
            type = "mountain"
            resourceId ?: return@setOnClickListener
            it.background = resources.getDrawable(resourceId)
            changeChoice(4)
        }


    }

    private fun changeChoice(index: Int) {
        if (index != 0) city_button.background = null
        if (index != 1) forest_button.background = null
        if (index != 2) field_button.background = null
        if (index != 3) lake_button.background = null
        if (index != 4) mountain_button.background = null
    }
}
