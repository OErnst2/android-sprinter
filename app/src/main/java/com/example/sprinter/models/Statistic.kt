package com.example.sprinter.models

import com.example.sprinter.models.Track
import java.util.*

class Statistic(val name: String,
                val length: Double,
                val timeSec: Int,
                val date: Date,
                val difficulty: String,
                var id: Int? = null,
                var location: String? = null,
                var track: Track? = null) {


    fun addIdAndReturn(number: Int): Statistic {
        id = number
        return this
    }


}