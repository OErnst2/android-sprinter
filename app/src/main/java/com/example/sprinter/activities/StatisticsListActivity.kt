package com.example.sprinter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sprinter.R
import com.example.sprinter.adapters.StatisticsAdapter
import com.example.sprinter.databases.StaticData
import kotlinx.android.synthetic.main.activity_statistics_list.*

class StatisticsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics_list)
        val adapter = StatisticsAdapter(StaticData.statistics)
        statistics_list_recycler.adapter = adapter
        statistics_list_recycler.layoutManager = LinearLayoutManager(this)
        statistics_list_recycler.visibility = View.VISIBLE
    }
}
