package com.michaelgallahancs.healthrewards

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.michaelgallahancs.healthrewards.utilities.CountdownTimer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    private val Context.dataStore by preferencesDataStore("settings")

    private val startingTime : String = "00:00:07"
    private val timerCheckPoints : Array<String> = arrayOf(startingTime, "00:00:05", "00:00:03")//arrayOf(startingTime, "36:00:00", "24:00:00")

    private lateinit var btnSubMiles : Button
    private lateinit var addButton: Button
    private lateinit var milesCounterText: TextView
    private lateinit var tvCountdown : TextView
    private lateinit var tvTokenCount : TextView
    private lateinit var linLayoutBackground : LinearLayout
    private lateinit var cbFood : CheckBox
    private lateinit var cbDrink : CheckBox
    private lateinit var tvCost : TextView

    // Initialize with time from storage
    private lateinit var timer : CountdownTimer
    private var cost : Int = 0

    private object Keys {
        val TIMER_CREATED = stringPreferencesKey("timer_created")
        val TOKEN_COUNT = stringPreferencesKey("token_count")
        val MILE_COUNT = stringPreferencesKey("mile_count")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        btnSubMiles = findViewById<Button>(R.id.btnSubMiles)
        addButton = findViewById<Button>(R.id.btnAddMiles)
        milesCounterText = findViewById<TextView>(R.id.tvMilesCounter)
        tvCountdown = findViewById<TextView>(R.id.tvCountdown)
        tvTokenCount = findViewById<TextView>(R.id.tvTokenCount)
        linLayoutBackground = findViewById<LinearLayout>(R.id.linLayoutBackground)
        cbFood = findViewById<CheckBox>(R.id.cbFood)
        cbDrink = findViewById<CheckBox>(R.id.cbDrink)
        tvCost = findViewById<TextView>(R.id.tvCost)

        // ##### Restore data from datastore #####
        lifecycleScope.launch() {
            val miles = getMilesFromDataStore()
            milesCounterText.text = if (miles != "") miles else "0"

            val tokens = getTokensFromDataStore()
            tvTokenCount.text = if (tokens != "") tokens else "0"

            val timerCreateTime = getTimerDateFromDataStore()
            timer = if(timerCreateTime != "")
                CountdownTimer(startingTime, LocalDateTime.parse(timerCreateTime), timerCheckpointCallback)
            else
                CountdownTimer(startingTime, timerCheckpointCallback)

            timer.setCheckpoints(timerCheckpointCallback, *timerCheckPoints)
            timer.start(tvCountdown)
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch() {
            saveAll()
        }
    }

    private val timerCheckpointCallback : (String) -> Unit = {
        // Alpha: 30
        // Green:  #6B4CAF42 76 175 80
        // Yellow: #4DFFEB3B 255 235 59
        // Red-Or: #4DF44336 244 67 54
        Log.d("healthapp", "callback has been executed: $it | match: ${it.matches("[0-9]+".toRegex())}")
        val tokenTotal = tvTokenCount.text.toString().toInt()

        if (it.matches("[0-9]+".toRegex())) {
            if (it.toString().toInt() <= tokenTotal)
                tvTokenCount.text = "${tokenTotal - it.toString().toInt()}"
            else
                tvTokenCount.text = "${0}"
        }

        if (it == timerCheckPoints[0])
            tvCountdown.setBackgroundColor(0x6B4CAF42)
        if (it == timerCheckPoints[1])
            tvCountdown.setBackgroundColor(0x4DFFEB3B)
        if (it == timerCheckPoints[2])
            tvCountdown.setBackgroundColor(0x4DF44336)
        if (it == "00:00:00") {
            if (tokenTotal > 0)
                tvTokenCount.text = "${tokenTotal - 1}"
            
            timer.restart()
        }
    }



    private suspend fun getMilesFromDataStore() : String {
        return dataStore.data.map { settings ->
            settings[Keys.MILE_COUNT] ?: ""
        }.first()
    }

    private suspend fun getTokensFromDataStore() : String {
        return dataStore.data.map { settings ->
            settings[Keys.TOKEN_COUNT] ?: ""
        }.first()
    }

    private suspend fun getTimerDateFromDataStore() : String {
        return dataStore.data.map { settings ->
            settings[Keys.TIMER_CREATED] ?: ""
        }.first()
    }

    private suspend fun saveAll() {
        dataStore.edit { settings ->
            settings[Keys.MILE_COUNT] = milesCounterText.text.toString()
            settings[Keys.TIMER_CREATED] = timer.getCreatedDateTime().toString()
            settings[Keys.TOKEN_COUNT] = tvTokenCount.text.toString()
        }
    }

    fun drinkChecked(view: View) {
        cbFood.isChecked = true
        cost =  if (cbFood.isChecked && cbDrink.isChecked) 2
                else if (cbFood.isChecked || cbDrink.isChecked) 1
                else 0
        tvCost.text = cost.toString()
    }

    fun foodChecked(view: View) {
        cost =  if (cbFood.isChecked && cbDrink.isChecked) 2
                else if (cbFood.isChecked || cbDrink.isChecked) 1
                else 0
        tvCost.text = cost.toString()
    }

    fun spendButtonClicked(view: View) {
        val tokenCount = tvTokenCount.text.toString().toInt()
        Log.d("healthapp", "$tokenCount")
        if (tokenCount >= cost)
            tvTokenCount.text = (tokenCount - cost).toString()
    }

    fun subtractMiles(view: View) {
        var result = milesCounterText.text.toString().toInt() - 1
        if (result >= 0)
            milesCounterText.text = ("${(milesCounterText.text.toString().toInt() - 1)}")
    }

    fun addMiles(view: View) {
        var result = milesCounterText.text.toString().toInt() + 1
        var tokenEarned = false
        if (result == 10) {
            tokenEarned = true
            result = 0
            timer.restart()
        }

        milesCounterText.text = "$result"
        if (tokenEarned)
            tvTokenCount.text = "${tvTokenCount.text.toString().toInt() + 1}"
    }
}