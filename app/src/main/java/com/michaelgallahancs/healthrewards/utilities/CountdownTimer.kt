package com.michaelgallahancs.healthrewards.utilities

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import kotlin.math.abs

class CountdownTimer(private val startingTime : String, private var checkpointCallback : ((checkpoint : String) -> Unit)? = null) {
    private var hours : Int = startingTime.split(":")[0].toInt()
    private var minutes : Int = startingTime.split(":")[1].toInt()
    private var seconds : Int = startingTime.split(":")[2].toInt()
    private lateinit var timerTextView : TextView
    private val template : String = "%02d:%02d:%02d"

    data class Checkpoint (var hours : Int, var minutes : Int, var seconds : Int)
    private var checkpoints : Array<Checkpoint>? = null

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

    constructor(startingTime: String, createdDateTime: LocalDateTime, checkpointCallback: ((checkpoint : String) -> Unit)? = null) : this(startingTime, checkpointCallback) {
        this.createdDateTime = createdDateTime

        var secondsRemaining = toSeconds(startingTime) - timePassed()
        if (secondsRemaining < 0) {
            var numTimersConsumed : Int = abs(secondsRemaining / toSeconds(startingTime))
            secondsRemaining = abs(secondsRemaining % toSeconds(startingTime))

            checkpointCallback?.let{ it("$numTimersConsumed") }
        }

        hours = secondsRemaining / 60 / 60
        secondsRemaining -= hours * 60 * 60
        minutes = secondsRemaining / 60
        secondsRemaining -= minutes * 60
        seconds = secondsRemaining
    }

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
            if ((hours + minutes + seconds) == 0) {
                handler.removeCallbacks(this)
                checkpointCallback?.let { it("00:00:00") }
            }

            timerTextView.text = timeRemainingString()

            // Call checkpoint call back when reached
            checkpoints?.forEach { checkpoint ->
                var checkpointReached : Boolean =   checkpoint.hours == hours
                                                    && checkpoint.minutes == minutes
                                                    && checkpoint.seconds == seconds

                if (checkpointReached)
                    checkpointCallback?.let { it(template.format(checkpoint.hours, checkpoint.minutes, checkpoint.seconds)) }
            }
        }
    }

    public fun setCheckpoints(vararg checkpoints: String) {
        this.checkpoints = Array(checkpoints.size) { Checkpoint(0,0,0) }
        checkpoints.forEachIndexed{ i, checkpoint ->
            val checkpointHours : Int = checkpoint.split(":")[0].toInt()
            val checkpointMinutes : Int = checkpoint.split(":")[1].toInt()
            val checkpointSeconds : Int = checkpoint.split(":")[2].toInt()

            this.checkpoints!![i].hours = checkpointHours
            this.checkpoints!![i].minutes = checkpointMinutes
            this.checkpoints!![i].seconds = checkpointSeconds

            // Fire each checkpoint that has been passed already
            if(timeRemaining() <= toSeconds(checkpointHours, checkpointMinutes, checkpointSeconds))
                checkpointCallback?.let{ it( template.format(this.checkpoints!![i].hours, this.checkpoints!![i].minutes, this.checkpoints!![i].seconds)) }
        }
    }

    public fun getCreatedDateTime() : LocalDateTime {
        return createdDateTime
    }

    public fun start(timerTextView : TextView) {
        this.timerTextView = timerTextView
        this.timerTextView.text = timeRemainingString()
        handler.postDelayed(countDownOneSec, 1000)
        checkpointCallback?.let{ it(timeRemainingString()) }
    }

    public fun restart() {
        stop()
        hours = startingTime.split(":")[0].toInt()
        minutes = startingTime.split(":")[1].toInt()
        seconds = startingTime.split(":")[2].toInt()
        createdDateTime = LocalDateTime.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // Month starts at 0
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )
        start(timerTextView)
    }

    private fun stop() {
        handler.removeCallbacks(countDownOneSec)
    }

    private fun timePassed() : Int {
        return ChronoUnit.SECONDS.between(createdDateTime, LocalDateTime.now()).toInt()
    }

    public fun timeRemainingString() : String {
        return template.format(hours, minutes, seconds)
    }

    private fun timeRemaining(): Int {
        return (hours * 60 * 60) + (minutes * 60) + seconds
    }

    private fun toSeconds(hours: Int, minutes: Int, seconds: Int): Int {
        return (hours * 60 * 60) + (minutes * 60) + seconds
    }

    private fun toSeconds(time: String): Int {
        val hours : Int = startingTime.split(":")[0].toInt()
        val minutes : Int = startingTime.split(":")[1].toInt()
        val seconds : Int = startingTime.split(":")[2].toInt()
        return (hours * 60 * 60) + (minutes * 60) + seconds
    }
}