package com.michaelgallahancs.healthrewards.utilities

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar

class CountdownTimer(private val startingTime : String) {
    private var hours : Int = startingTime.split(":")[0].toInt()
    private var minutes : Int = startingTime.split(":")[1].toInt()
    private var seconds : Int = startingTime.split(":")[2].toInt()
    private lateinit var timerTextView : TextView
    private val template : String = "%02d:%02d:%02d"

    data class Checkpoint (var hours : Int, var minutes : Int, var seconds : Int)
    private var checkpoints : Array<Checkpoint>? = null
    private var checkpointCallback : ((checkpoint : String) -> Unit)? = null

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

    constructor(startingTime: String, createdDateTime: LocalDateTime) : this(startingTime) {
        this.createdDateTime = createdDateTime

        var actualSecRemaining = secondsRemaining() - timePassedInSeconds()
        hours = actualSecRemaining / 60 / 60
        actualSecRemaining -= hours * 60 * 60
        minutes = actualSecRemaining / 60
        actualSecRemaining -= minutes * 60
        seconds = actualSecRemaining
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
                checkpointCallback?.let { it("00:00:00") }
                handler.removeCallbacks(this)
            }

            timerTextView.text = timeRemainingToString()


            checkpoints?.forEach { checkpoint ->
                var checkpointReached : Boolean =   checkpoint.hours == hours
                                                    && checkpoint.minutes == minutes
                                                    && checkpoint.seconds == seconds

                if (checkpointReached)
                    checkpointCallback?.let { it(template.format(checkpoint.hours, checkpoint.minutes, checkpoint.seconds)) }
            }
        }
    }

    public fun setCheckpoints(callback: (String) -> Unit, vararg checkpoints: String) {
        checkpointCallback = callback
        this.checkpoints = Array(checkpoints.size) { Checkpoint(0,0,0) }
        checkpoints.forEachIndexed{ i, checkpoint ->
            this.checkpoints!![i].hours = checkpoint.split(":")[0].toInt()
            this.checkpoints!![i].minutes = checkpoint.split(":")[1].toInt()
            this.checkpoints!![i].seconds = checkpoint.split(":")[2].toInt()

            if(secondsRemaining() < toSeconds(this.checkpoints!![i].hours, this.checkpoints!![i].minutes, this.checkpoints!![i].seconds))
                checkpointCallback?.let{ it( template.format(this.checkpoints!![i])) }
        }
    }

    public fun getCreatedDateTime() : LocalDateTime {
        return createdDateTime
    }


    public fun start(timerTextView : TextView) {
        this.timerTextView = timerTextView
        this.timerTextView.text = timeRemainingToString()
        handler.postDelayed(countDownOneSec, 1000)
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

    private fun toSeconds(hours: Int, minutes: Int, seconds: Int): Int {
        return (hours * 60 * 60) + (minutes * 60) + seconds
    }
}