package com.michaelgallahancs.healthrewards.utilities

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.util.rangeTo
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date

class CountdownTimer(initialTimeStr : String) {
    private var hours : Int = initialTimeStr.split(":")[0].toInt()
    private var minutes : Int = initialTimeStr.split(":")[1].toInt()
    private var seconds : Int = initialTimeStr.split(":")[2].toInt()
    private lateinit var timerTextView : TextView
    val template : String = "%02d:%02d:%02d"

    private val calendar : Calendar = Calendar.getInstance()
    private var createdDateTime : LocalDateTime = LocalDateTime.of(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1, // Month starts at 0
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND)
    )

    val handler: Handler = Handler(Looper.getMainLooper())
    private val countDownOneSec : Runnable = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 1000)

            seconds -= 1
            if (seconds < 0) {
                seconds = 59
                minutes -= 1
            }
            if (minutes < 0) {
                minutes = 59
                hours -= 1
            }
            if ((hours + minutes + seconds) == 0)
                handler.removeCallbacks(this)

            timerTextView.text = template.format(hours,minutes,seconds)
        }
    }

    public fun getCreatedDateTime() : LocalDateTime {
        return createdDateTime
    }


    public fun start(timerTextView : TextView) {
        this.timerTextView = timerTextView
        handler.post(countDownOneSec)
    }

    public fun reset(timerTextView : TextView) {
        handler.removeCallbacks(countDownOneSec)
        this.timerTextView = timerTextView
        hours = 48
        minutes = 0
        seconds = 0
        createdDateTime = LocalDateTime.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // Month starts at 0
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )
        this.timerTextView.text = timeRemainingToString()
        handler.post(countDownOneSec)
    }

    public fun stop() {
        handler.removeCallbacks(countDownOneSec)
    }

    private fun timePassedInSeconds() : String {
        return ChronoUnit.SECONDS.between(createdDateTime, LocalDateTime.now()).toString()
    }

    public fun timeRemainingToString() : String {
        return template.format(hours, minutes, seconds)
    }

    fun secondsRemaining(): Int {
        return (hours * 60 * 60) + (minutes * 60) + seconds
    }
}