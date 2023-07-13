package com.michaelgallahancs.healthrewards.utilities

class Countdown(timerString : String) {
    private var timerStr : String = timerString

    fun toSeconds(): Int {
        val timerComponents : List<String> = timerStr.split(":")
        return (timerComponents[0].toInt() * 60 * 60) + // hours
               (timerComponents[1].toInt() * 60) +      // minutes
               timerComponents[2].toInt()               // seconds
    }
}