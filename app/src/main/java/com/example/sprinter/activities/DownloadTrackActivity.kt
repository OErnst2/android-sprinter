package com.example.sprinter.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sprinter.R
import com.example.sprinter.activities.MainActivity.Companion.units
import com.example.sprinter.databases.StaticData
import com.example.sprinter.models.Track
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_download_track.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DownloadTrackActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var track: Track

    private lateinit var mMap: GoogleMap

    private var resourceId: Int? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_track)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        val index = intent.getIntExtra("index", 0)
        track = StaticData.onlineTracks[index]

        this.resourceId = resources?.getIdentifier(
            "ic_star_gold_full", "drawable",
            packageName
        )
        val resourceId = resources?.getIdentifier(
            track.img, "drawable",
            packageName
        )
        resourceId ?: return
        icon.setImageDrawable(resources.getDrawable(resourceId))

        val rating = StaticData.getRateValue(track.index)
        if (rating != null) {
            fillStars(rating)
        }

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
        if (track.rating != 0.0) {
            rating_view.text = (100.0 * ((track.rating - 1.0) / 4.0)).roundToInt().toString() + " %"
        } else {
            rating_view.text = "-"
        }

        download_button.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val x = GlobalScope.async {
                    return@async StaticData.saveTrack(track)
                }
                x.await()
                val message =
                    if (x.getCompleted()) getString(R.string.saved) else getString(R.string.operation_unsuccessful)
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

            }
        }


        star_one.setOnClickListener { sendEvaluation(1) }
        star_two.setOnClickListener { sendEvaluation(2) }
        star_three.setOnClickListener { sendEvaluation(3) }
        star_four.setOnClickListener { sendEvaluation(4) }
        star_five.setOnClickListener { sendEvaluation(5) }



        mapFragment.getMapAsync(this)
    }

    private fun fillStars(value: Int) {
        TransitionManager.beginDelayedTransition(stars)
        val resourceId = this.resourceId
        resourceId ?: return
        if (value >= 1) star_one.setImageDrawable(resources.getDrawable(resourceId))
        if (value >= 2) star_two.setImageDrawable(resources.getDrawable(resourceId))
        if (value >= 3) star_three.setImageDrawable(resources.getDrawable(resourceId))
        if (value >= 4) star_four.setImageDrawable(resources.getDrawable(resourceId))
        if (value >= 5) star_five.setImageDrawable(resources.getDrawable(resourceId))

    }


    private fun sendEvaluation(value: Int) {
        if (StaticData.isIdInDatabase(track.index)) {
            Toast.makeText(this, getString(R.string.already_rated), Toast.LENGTH_LONG).show()
            return
        }
        Ion.with(this).load("https://sprinter-server.000webhostapp.com/evaluate.php?index=${track.index}&value=$value")
            .asString(Charsets.UTF_8)
            .setCallback { e, result ->
                if (result != null) {
                    try {
                        rating_view.text = (100.0 * ((result.toDouble() - 1.0) / 4.0)).roundToInt().toString() + " %"
                        StaticData.addEvaluation(track.index, value)
                        fillStars(value)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "Operation unsuccessful", Toast.LENGTH_LONG).show()
                }
            }
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
