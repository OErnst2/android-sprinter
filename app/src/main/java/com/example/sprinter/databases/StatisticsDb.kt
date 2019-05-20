package com.example.sprinter.databases

import com.example.sprinter.models.Statistic
import java.util.*
import kotlin.collections.ArrayList

class StatisticsDb(list: ArrayList<Statistic>) : ArrayList<Statistic>(list) {

    constructor(): this(ArrayList())

    /**
     * initialize StatisticsDb from local database
     * returns true if succeeded, false otherwise
     */
    fun initializeFromDb(): Boolean {
        return false
    }

    fun filterStatistics(predicate: (Statistic) -> Boolean): StatisticsDb {
        return StatisticsDb(filter(predicate) as ArrayList<Statistic>)
    }

}