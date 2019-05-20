package com.example.sprinter.databases

import android.content.Context
import com.koushikdutta.ion.Ion
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.Log
import com.example.sprinter.models.Statistic
import com.example.sprinter.models.Track
import java.util.*

class StaticData {

    companion object {
        lateinit var statistics: StatisticsDb
            private set
        lateinit var tracks: TracksDb
            private set
        lateinit var onlineTracks: TracksDb
            private set
        lateinit var mydatabase: SQLiteDatabase


        fun prepareDatabaseAndTables(context: Context) {
            StaticData.mydatabase = context.openOrCreateDatabase("db", Context.MODE_PRIVATE, null)
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Statistics(runKm VARCHAR, runTime VARCHAR, averageSpeed REAL, difficulty VARCHAR, trackName VARCHAR);")
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Tracks(name VARCHAR,points VARCHAR, rating REAL, difficulty VARCHAR, img VARCHAR, length REAL);")
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Evaluated(trackId INTEGER, value INTEGER);")
        }


        fun addEvaluation(id: Int, value: Int) {
            mydatabase.execSQL("INSERT INTO Evaluated VALUES('$id', '$value');")

        }

        fun isIdInDatabase(id: Int) : Boolean {
            val resultSet = mydatabase.rawQuery("Select * from Evaluated WHERE trackId = '$id'", null)
            if (resultSet.moveToFirst()) {
                resultSet.close()
                return true
            }
            return false
        }

        fun getRateValue(id: Int) : Int? {
            val resultSet = mydatabase.rawQuery("Select * from Evaluated WHERE trackId = '$id'", null)
            if (resultSet.moveToFirst()) {

                val result = resultSet.getInt(resultSet.getColumnIndex("value"))
                resultSet.close()
                return result
            }
            return null
        }



        fun downloadTracks(context: Context?, procedure: () -> Unit): Boolean {
            Ion.with(context).load("https://sprinter-server.000webhostapp.com/tracks.php").asString(Charsets.UTF_8)
                .setCallback { e, result ->
                    if (result != null) {
                        onlineTracks = TracksDb(result)
                        procedure()
                    }
                }
            return true
        }

        fun updateStatistics(one: Statistic) {
            val average = one.length / (one.timeSec / 3600)
            mydatabase.execSQL("INSERT INTO Statistics VALUES('${one.length}','${one.timeSec}','$average','${one.difficulty}', '${one.name}');")
        }

        fun saveTrack(one: Track?): Boolean {
            one ?: return false
            val resultSet = mydatabase.rawQuery("Select * from Tracks WHERE name = '${one.name}'", null)
            if (resultSet.moveToFirst()) {
                resultSet.close()
                return false
            }
            var points = ""
            for (point in one.points) {
                points += point.latitude.toString() + ":" + point.longitude.toString() + ";"
            }
            mydatabase.execSQL("INSERT INTO Tracks VALUES('${one.name}','${points}','${one.rating}','${one.difficulty}','${one.img}', '${one.length}');")

            resultSet.close()
            return true

        }


        fun deleteTrackFromDatabase(name: String) {
            mydatabase.execSQL("DELETE FROM Tracks WHERE name='$name'")
        }


        fun initializeTracksFromDatabase() {
            var data = ""

            val cursor = mydatabase.rawQuery("Select * from Tracks", null)
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val name = cursor.getString(cursor.getColumnIndex("name"))
                    data = data + cursor.getString(cursor.getColumnIndex("name")) + ";" +
                            cursor.getString(cursor.getColumnIndex("difficulty")) + ";" +
                            cursor.getDouble(cursor.getColumnIndex("rating")).toString() + ";" + "0;0;" +
                            cursor.getString(cursor.getColumnIndex("img")) + ";" +
                            cursor.getDouble(cursor.getColumnIndex("length")).toString() + ";" +
                            cursor.getString(cursor.getColumnIndex("points")) + "\n"
                    Log.d("DATABASE", name)
                    cursor.moveToNext()
                }
            }
            tracks = TracksDb(data)
            cursor.close()
        }

        fun deleteStatisticFromDb(id: Int) {
            mydatabase.execSQL("DELETE FROM Statistics WHERE rowid='$id'")
            statistics = statistics.filterStatistics { it.id != id }
        }


        fun initializeStatisticsFromDatabase() {

            val cursor = mydatabase.rawQuery("Select rowid, * from Statistics", null)
            statistics = StatisticsDb()
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    Log.d("adding", "one")
                    println("id id id +" + cursor.getInt(cursor.getColumnIndex("rowid")))
                    statistics.add(
                        Statistic(
                            cursor.getString(cursor.getColumnIndex("trackName")),
                            cursor.getString(cursor.getColumnIndex("runKm")).toDouble(),
                            cursor.getString(cursor.getColumnIndex("runTime")).toInt(),
                            Date(1999, 10, 10),
                            cursor.getString(cursor.getColumnIndex("difficulty"))
                        ).addIdAndReturn(
                            cursor.getInt(cursor.getColumnIndex("rowid"))

                        )
                    )
                    cursor.moveToNext()
                }
            }
            cursor.close()
        }

    }

    class TracksDatabaseInitializer(private val procedure: () -> Unit) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            initializeTracksFromDatabase()
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            procedure()
        }
    }

    class StatisticsDatabaseInitializer(private val procedure: () -> Unit) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            initializeStatisticsFromDatabase()
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            procedure()
        }
    }
}