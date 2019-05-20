package com.example.sprinter.activities


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sprinter.R
import com.example.sprinter.adapters.OnlineTracksAdapter
import com.example.sprinter.databases.StaticData
import kotlinx.android.synthetic.main.activity_my_tracks.*


class TracksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracks_fragment)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.visibility = View.VISIBLE
        loadRecycler()

    }

    private fun loadRecycler() {
        StaticData.downloadTracks(this) {
            recycler_view.adapter = OnlineTracksAdapter(StaticData.onlineTracks, this)
        }
    }


    override fun onResume() {
        super.onResume()
        loadRecycler()
    }
}