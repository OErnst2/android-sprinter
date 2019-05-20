package com.example.sprinter.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sprinter.activities.DownloadTrackActivity
import com.example.sprinter.R
import com.example.sprinter.models.Track
import kotlinx.android.synthetic.main.tracks_fragment.view.*
import kotlin.math.roundToInt

import android.annotation.SuppressLint
import com.example.sprinter.activities.MainActivity.Companion.units
import com.example.sprinter.activities.StartActivity


class OnlineTracksAdapter(private val tracks: List<Track>, private val context: Context?) :
    RecyclerView.Adapter<OnlineTracksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)

        view.setOnClickListener {
            val itemPosition = parent.recycler_view.getChildLayoutPosition(view)
            val intent = Intent(parent.context, DownloadTrackActivity::class.java)

            intent.putExtra("index", itemPosition)

            context?.startActivity(intent)
        }

        return ViewHolder(view, context)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tracks[position])
    }


    override fun getItemCount(): Int {
        return tracks.size
    }

    class ViewHolder(itemView: View, private val context: Context?) : RecyclerView.ViewHolder(itemView) {

        var icon: ImageView = itemView.findViewById(R.id.icon)
        var name: TextView = itemView.findViewById(R.id.name)
        var item: LinearLayout = itemView.findViewById(R.id.item)
        var length: TextView = itemView.findViewById(R.id.length)
        var rating: TextView = itemView.findViewById(R.id.rating)
        var difficulty: TextView = itemView.findViewById(R.id.difficulty)

        @SuppressLint("SetTextI18n")
        fun bind(track: Track) {
            //icon.setImageResource(R.drawable.ic_launcher_foreground)

            val resources = context?.resources
            val resourceId = resources?.getIdentifier(
                track.img, "drawable",
                context?.packageName
            )
            resourceId ?: return
            icon.setImageDrawable(resources.getDrawable(resourceId))
            name.text = track.name
            if (units == "Mil") {
                length.text = String.format("%.2f", track.length * StartActivity.KM_TO_MILES) + " $units"
            } else {
                length.text = (track.length).toString() + " $units"
            }
            if (track.rating != 0.0) {
                rating.text = "${context?.getString(R.string.rating)}: " + (((track.rating - 1.0) / 4.0) * 100).roundToInt().toString() + " %"
            } else {
                rating.text = "-"
            }

            var difficultyText = context?.getString(R.string.hard)
            if (track.difficulty == "easy") {
                difficultyText = context?.getString(R.string.easy)
            } else if (track.difficulty == "medium") {
                difficultyText = context?.getString(R.string.medium)
            }

            difficulty.text = "${context?.getString(R.string.difficulty)}: " + difficultyText
        }
    }
}