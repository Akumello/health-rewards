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

class CountdownTimer(startingTime : String) {
    private var hours : Int = startingTime.split(":")[0].toInt()
    private var minutes : Int = startingTime.split(":")[1].toInt()
    private var seconds : Int = startingTime.split(":")[2].toInt()
    private lateinit var timerTextView : TextView
    private val template : String = "%02d:%02d:%02d"
    private lateinit var callback : (secondsLeft : Int) -> Unit

    private val calendar : Calendar = Calendar.getInstance()
    private var createdDateTime : LocalDateTime = LocalDateTime.of(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1, // Month starts at 0
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND)
    )

    constructor(startingTime: String, createdDateTime: LocalDateTime) : this(startingTime) {
        Log.d("healthapp", "Setting to $createdDateTime. \nCurrent time is ${this.createdDateTime}")
        this.createdDateTime = createdDateTime

        Log.d("healthapp", "Time passed is ${timePassedInSeconds()}\nSeconds remaining is ${secondsRemaining()}")
        var newSecondsRemaining = secondsRemaining() - timePassedInSeconds()
        hours = newSecondsRemaining / 60 / 60
        newSecondsRemaining -= hours * 60 * 60
        minutes = newSecondsRemaining / 60
        newSecondsRemaining -= minutes * 60
        seconds = newSecondsRemaining
        Log.d("healthapp", "After update, seconds remaining is ${secondsRemaining()}")
    }

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

            timerTextView.text = timeRemainingToString()
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

    private fun timePassedInSeconds() : Int {
        return ChronoUnit.SECONDS.between(createdDateTime, LocalDateTime.now()).toInt()
    }

    public fun timeRemainingToString() : String {
        return template.format(hours, minutes, seconds)
    }

    private fun secondsRemaining(): Int {
        return (hours * 60 * 60) + (minutes * 60) + seconds
    }
}