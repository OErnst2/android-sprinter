package com.example.sprinter.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sprinter.R
import com.example.sprinter.activities.MainActivity.Companion.units
import com.example.sprinter.classes.round
import com.example.sprinter.databases.StaticData
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private var totalLength: Double = 0.0
    private var totalTimeHours: Double = 0.0
    private var count: Int = 0
    private var averageSpeed: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        StaticData.StatisticsDatabaseInitializer {
            processAllStatisticsAsync()
            all_stat_button.setOnClickListener {
                startActivity(Intent(this, StatisticsListActivity::class.java))
            }
        }.execute()
    }


    override fun onResume() {
        super.onResume()
        processAllStatisticsAsync()

    }

    @SuppressLint("SetTextI18n")
    private fun processAllStatisticsAsync() = GlobalScope.launch(Dispatchers.Main) {

        val x = GlobalScope.launch {
            totalLength = StaticData.statistics.sumByDouble { it.length }.round(2)
            totalTimeHours = (StaticData.statistics.sumBy { it.timeSec }.toDouble() / 3600).round(2)
            count = StaticData.statistics.size
            averageSpeed = (totalLength / if (totalTimeHours == 0.0) 1.0 else totalTimeHours).round(2)
        }
        x.join()
        if (units == "Mil") {
            run_km.text = "${String.format("%.2f", totalLength * StartActivity.KM_TO_MILES)} $units"
            average_speed.text = "${String.format("%.3f", averageSpeed * StartActivity.KM_TO_MILES)} $units/h"
        } else {
            run_km.text = "$totalLength $units"
            average_speed.text = "$averageSpeed $units/h"
        }
        run_time.text = "$totalTimeHours h"
        run_tracks.text = count.toString()
    }

}
