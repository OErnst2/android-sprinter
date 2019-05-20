package com.example.sprinter.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sprinter.R
import com.example.sprinter.activities.MainActivity.Companion.units
import com.example.sprinter.activities.StartActivity
import com.example.sprinter.classes.round
import com.example.sprinter.classes.stanardString
import com.example.sprinter.databases.StaticData
import com.example.sprinter.models.Statistic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.launch

class StatisticsAdapter(private var statistics: List<Statistic>) : RecyclerView.Adapter<StatisticsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_statistics,
                parent,
                false
            ),
            this, parent.context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(statistics[position], position)

    }

    private fun deleteItem(position: Int) {
        statistics = StaticData.statistics
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, StaticData.statistics.size)

    }


    override fun getItemCount(): Int {
        return statistics.size
    }
    class ViewHolder(itemView: View, private val outer: StatisticsAdapter, private val context : Context?) : RecyclerView.ViewHolder(itemView) {


        var name: TextView = itemView.findViewById(R.id.name)
        var item: LinearLayout = itemView.findViewById(R.id.item)
        var averageSpeed: TextView = itemView.findViewById(R.id.averageSpeed)
        var length: TextView = itemView.findViewById(R.id.length)
        var timeSec: TextView = itemView.findViewById(R.id.timeSec)
        var difficulty: TextView = itemView.findViewById(R.id.difficulty)
        var date: TextView = itemView.findViewById(R.id.date)
        var delete_button: ImageButton = itemView.findViewById(R.id.delete_button)



        @SuppressLint("SetTextI18n")
        fun bind(track : Statistic, position: Int) {
            if (track.name == "Custom") {
                name.text = context?.getText(R.string.custom)
            } else {
                name.text = track.name
            }
            date.text = track.date.stanardString()
            if (units == "Mil") {
                length.text = "${context?.getString(R.string.length)}: " + String.format("%.2f", track.length * StartActivity.KM_TO_MILES) + " $units"
                averageSpeed.text = "${context?.getString(R.string.average_speed)}: " + (track.length * StartActivity.KM_TO_MILES /  (track.timeSec.toDouble() / 3600)).round(2).toString() + " $units/h"
            } else {
                length.text = "${context?.getString(R.string.length)}: " + track.length + " $units"
                averageSpeed.text = "${context?.getString(R.string.average_speed)}: " + (track.length / (track.timeSec.toDouble() / 3600)).round(2).toString() + " $units/h"
            }
            timeSec.text = "${context?.getString(R.string.time)}: " + (track.timeSec.toDouble() / 3600).round(2).toString() + " h"

            var difficultyText = context?.getString(R.string.hard)
            if (track.difficulty == "easy") {
                difficultyText = context?.getString(R.string.easy)
            } else if (track.difficulty == "medium") {
                difficultyText = context?.getString(R.string.medium)
            }
            difficulty.text = "${context?.getString(R.string.difficulty)}: " + difficultyText

            delete_button.setOnClickListener {

                GlobalScope.launch(Dispatchers.Main) {
                    val x = GlobalScope.launch {
                        val id = track.id ?: return@launch
                        StaticData.deleteStatisticFromDb(id)
                    }
                    x.join()
                    outer.deleteItem(position)
                    TransitionManager.beginDelayedTransition(item)
                }

            }

        }
    }
}