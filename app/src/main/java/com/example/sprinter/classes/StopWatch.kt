package com.example.sprinter.classes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.TextView
import com.example.sprinter.R

class StopWatch(val context : Context) {


    private var miliSecondTime : Long = 0
    private var startTime : Long = 0
    private var timeBuff : Long = 0
    private var updateTime : Long = 0

    var seconds = 0
        private set

    private var miliSeconds = 0

    var minutes = 0
        private set

    private var handler : Handler = Handler()

    private var timeRunnable:Runnable = object:Runnable {

        @SuppressLint("SetTextI18n")
        override fun run() {
            miliSecondTime = SystemClock.uptimeMillis() - startTime
            updateTime = timeBuff + miliSecondTime
            seconds = (updateTime / 1000).toInt()
            minutes = seconds / 60
            seconds %= 60
            miliSeconds = (updateTime % 1000).toInt()
            handler.postDelayed(this, 0)
            (context as Activity).findViewById<TextView>(R.id.timeTextView)
                .text = "$minutes:${String.format("%02d", seconds)}:${String.format("%03d", miliSeconds)}"

        }
    }

    fun startstopWatch() {
        startTime = SystemClock.uptimeMillis()
           handler.postDelayed(timeRunnable, 0)
    }

    fun pauseClock() {
        timeBuff += miliSecondTime
        handler.removeCallbacks(timeRunnable)
    }

    fun endClock() {
        handler.removeCallbacks(timeRunnable)
        miliSecondTime = 0
        startTime = 0
        updateTime = 0
        seconds = 0
        miliSeconds = 0
        minutes = 0
        (context as Activity).findViewById<TextView>(R.id.timeTextView).text = "00:00:00"
    }
}