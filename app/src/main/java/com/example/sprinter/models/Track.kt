package com.example.sprinter.models

import com.example.sprinter.classes.minus
import com.google.android.gms.maps.model.LatLng


class Track() {
    var length: Double = 0.0
        private set
    var points: ArrayList<LatLng> = ArrayList()
        private set
    var name: String = ""
        private set
    var difficulty: String = "easy"
        private set
    var rating: Double = 0.0
        private set
    var numberOfEvaluators: Int = 0
        private set
    var index: Int = 0
        private set
    var img: String = "city"
        private set



    constructor(length: Double, points: ArrayList<LatLng>) : this() {
        this.length = length
        this.points = ArrayList(points)
    }

    constructor(line: String) : this() {
        points = ArrayList()
        val parts = line.split(";")
        name = parts[0]
        difficulty = parts[1]
        rating = parts[2].toDouble()
        numberOfEvaluators = parts[3].toInt()
        index = parts[4].toInt()
        img = parts[5]
        length = parts[6].toDouble()
        for (i in 7 until parts.size) {
            if (parts[i].length < 3) {
                continue
            }
            val coordinates = parts[i].split(":")
            points.add(LatLng(coordinates[0].toDouble(), coordinates[1].toDouble()))
        }
    }







    fun countLength(): Double {
        if (points.size < 2) {
            return 0.0
        }
        var sum = 0.0
        for (i in 1 until points.size) {
            sum += points[i] - points[i - 1]
        }
        return sum
    }


}