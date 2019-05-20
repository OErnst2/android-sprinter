package com.example.sprinter.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.sprinter.R
import com.example.sprinter.activities.MainActivity.Companion.units
import com.example.sprinter.models.Track
import com.example.sprinter.databases.StaticData
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_track_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TrackScreenActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var track: Track

    private lateinit var mMap: GoogleMap

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_screen)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        val index = intent.getIntExtra("index", 0)
        track = StaticData.tracks[index]


        val resources = resources
        val resourceId = resources?.getIdentifier(
            track.img, "drawable",
            packageName
        )
        resourceId ?: return
        icon.setImageDrawable(resources.getDrawable(resourceId))



        name_view.text = track.name
        if (units == "Mil") {
            length_view.text = "${getString(R.string.length)}: ${String.format(
                "%.2f",
                track.length * StartActivity.KM_TO_MILES
            )} $units"
        } else {
            length_view.text = "${getString(R.string.length)}: ${track.length} $units"
        }
        var difficultyText = getString(R.string.hard)
        if (track.difficulty == "easy") {
            difficultyText = getString(R.string.easy)
        } else if (track.difficulty == "medium") {
            difficultyText = getString(R.string.medium)
        }
        difficulty_view.text = "${getString(R.string.difficulty)}: $difficultyText"

        start_button.setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java).putExtra("index", index))
        }

        delete_button.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val x = GlobalScope.async {
                    StaticData.deleteTrackFromDatabase(track.name)
                }
                x.await()
                Toast.makeText(applicationContext, getString(R.string.deleted), Toast.LENGTH_SHORT).show()
                finish()
            }
        }



        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLoadedCallback {

            mMap.clear()
            mMap.addPolyline(PolylineOptions().addAll(track.points).color(R.color.abc_primary_text_material_dark))

            if (track.points.size > 0) {
                val boundBuilder = LatLngBounds.builder()
                for (point: LatLng in track.points) {
                    boundBuilder.include(point)
                }
                val bounds = boundBuilder.build()
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
            }
        }


    }

}
