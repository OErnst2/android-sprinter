package com.example.sprinter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sprinter.R
import com.example.sprinter.adapters.TracksAdapter
import com.example.sprinter.databases.StaticData
import kotlinx.android.synthetic.main.activity_my_tracks.*

class MyTracksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tracks)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.visibility = View.VISIBLE
        loadRecycler()

    }


    private fun loadRecycler() {
        StaticData.TracksDatabaseInitializer {
            recycler_view.adapter = TracksAdapter(StaticData.tracks, this)
        }.execute()
    }


    override fun onResume() {
        super.onResume()
        loadRecycler()
    }

}
