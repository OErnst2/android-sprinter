package com.example.sprinter.databases

import com.example.sprinter.models.Track

class TracksDb(data: String) : ArrayList<Track>() {
    init {
        if (data != "") {
            val lines = data.split("\n")
            for (line in lines) {
                if (line.length > 5) {
                    if (line.trim() == "<html>") break //there is an error on the server and so nothing happens
                    add(Track(line))
                }

            }
        }

    }
}