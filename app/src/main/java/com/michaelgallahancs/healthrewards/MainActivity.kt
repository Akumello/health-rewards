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
import androidx.compose.ui.input.key.Key
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.michaelgallahancs.healthrewards.utilities.CountdownTimer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime

//TODO: Save and restore clickable states
//      Set to clickable again on start of next day

class MainActivity : ComponentActivity() {
    private val Context.dataStore by preferencesDataStore("settings")

    private val startingTime : String = "48:00:00"
    private val timerCheckPoints : Array<String> = arrayOf(startingTime, "36:00:00", "24:00:00")
    private val hourOfNewDay : Int = 4 // e.g. 4 for 4am or 20 for 8pm

    private lateinit var btnSubMiles : Button
    private lateinit var addButton: Button
    private lateinit var milesCounterText: TextView
    private lateinit var tvCountdown : TextView
    private lateinit var tvTokenCount : TextView
    private lateinit var linLayoutBackground : LinearLayout
    private lateinit var cbFood : CheckBox
    private lateinit var cbDrinks : CheckBox
    private lateinit var cbSweets : CheckBox
    private lateinit var tvCost : TextView

    // Initialize with time from storage
    private lateinit var timer : CountdownTimer
    private var cost : Int = 0

    private object Keys {
        val TIMER_CREATED = stringPreferencesKey("timer_created")
        val TOKEN_COUNT = stringPreferencesKey("token_count")
        val MILE_COUNT = stringPreferencesKey("mile_count")
        val LAST_DAY_OF_YEAR = stringPreferencesKey("last_day")
        val FOOD_STATE = stringPreferencesKey("food_state")
        val DRINKS_STATE = stringPreferencesKey("drinks_state")
        val SWEETS_STATE = stringPreferencesKey("sweets_state")
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
        cbDrinks = findViewById<CheckBox>(R.id.cbDrinks)
        cbSweets = findViewById<CheckBox>(R.id.cbSweets)
        tvCost = findViewById<TextView>(R.id.tvCost)
    }

    override fun onStart() {
        super.onStart()

        // ##### Restore data from datastore #####
        lifecycleScope.launch() {
            Log.d("healthapp", "Today: ${LocalDateTime.now().dayOfYear} | Saved: ${getStringFromDataStore(Keys.LAST_DAY_OF_YEAR)}")
            if (LocalDateTime.now().dayOfYear == getStringFromDataStore(Keys.LAST_DAY_OF_YEAR).toInt()) {
                cbFood.isClickable = getStringFromDataStore(Keys.FOOD_STATE).toInt() == 1
                cbDrinks.isClickable = getStringFromDataStore(Keys.DRINKS_STATE).toInt() == 1
                cbSweets.isClickable = getStringFromDataStore(Keys.SWEETS_STATE).toInt() == 1

                cbFood.alpha = 1 - 0.5f * (!cbFood.isClickable).toInt()
                cbDrinks.alpha = 1 - 0.5f * (!cbDrinks.isClickable).toInt()
                cbSweets.alpha = 1 - 0.5f * (!cbSweets.isClickable).toInt()
            }

            val miles = getStringFromDataStore(Keys.MILE_COUNT)
            milesCounterText.text = if (miles != "") miles else "0"

            val tokens = getStringFromDataStore(Keys.TOKEN_COUNT)
            tvTokenCount.text = if (tokens != "") tokens else "0"

            val timerCreateTime = getStringFromDataStore(Keys.TIMER_CREATED)
            timer = if(timerCreateTime != "")
                CountdownTimer(startingTime, LocalDateTime.parse(timerCreateTime), timerCheckpointCallback)
            else
                CountdownTimer(startingTime, timerCheckpointCallback)

            timer.setCheckpoints(*timerCheckPoints)
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
        //Log.d("healthapp", "callback has been executed: $it | match: ${it.matches("[0-9]+".toRegex())}")
        val tokenTotal = tvTokenCount.text.toString().toInt()

        // Subtract from tokens the number of timers consumed since last the app was opened last
        if (it.matches("[0-9]+".toRegex())) {
            if (it.toString().toInt() <= tokenTotal)
                tvTokenCount.text = "${tokenTotal - it.toString().toInt()}"
            else
                tvTokenCount.text = "${0}"
        }

        if (it == timerCheckPoints[0])
            tvCountdown.setBackgroundColor(0x6B4CAF42) // Green
        if (it == timerCheckPoints[1])
            tvCountdown.setBackgroundColor(0x4DFFEB3B) // Yellow
        if (it == timerCheckPoints[2])
            tvCountdown.setBackgroundColor(0x4DF44336) // Red
        if (it == "00:00:00") {
            if (tokenTotal > 0)
                tvTokenCount.text = "${tokenTotal - 1}"

            timer.restart()
        }
    }

    private suspend fun getStringFromDataStore(key : Preferences.Key<String>) : String {
        return dataStore.data.map { settings ->
            settings[key] ?: ""
        }.first()
    }

    private suspend fun saveAll() {
        dataStore.edit { settings ->
            settings[Keys.MILE_COUNT] = milesCounterText.text.toString()
            settings[Keys.TIMER_CREATED] = timer.getCreatedDateTime().toString()
            settings[Keys.TOKEN_COUNT] = tvTokenCount.text.toString()
            settings[Keys.LAST_DAY_OF_YEAR] = LocalDateTime.now().dayOfYear.toString()
            settings[Keys.FOOD_STATE] = cbFood.isClickable.toInt().toString()
            settings[Keys.DRINKS_STATE] = cbDrinks.isClickable.toInt().toString()
            settings[Keys.SWEETS_STATE] = cbSweets.isClickable.toInt().toString()
        }
    }

    private fun Boolean.toInt() = if (this) 1 else 0

    fun drinkChecked(view: View) {
        if (cbFood.isClickable)
            cbFood.isChecked = true

        setCost()
    }

    fun foodChecked(view: View) {
        if (cbDrinks.isChecked)
            cbDrinks.isChecked = false

        setCost()
    }

    fun sweetsChecked(view: View) {
        setCost()
    }

    private fun setCost() {
        cost = cbFood.isChecked.toInt() + cbDrinks.isChecked.toInt() + cbSweets.isChecked.toInt()
        tvCost.text = cost.toString()
    }

    fun spendButtonClicked(view: View) {
        val tokenCount = tvTokenCount.text.toString().toInt()
        if (tokenCount >= cost) {
            tvTokenCount.text = (tokenCount - cost).toString()
            cbFood.isClickable = !cbFood.isChecked && cbFood.isClickable
            cbDrinks.isClickable = !cbDrinks.isChecked && cbDrinks.isClickable
            cbSweets.isClickable = !cbSweets.isChecked && cbSweets.isClickable

            cbFood.alpha = 1 - 0.5f * (!cbFood.isClickable).toInt()
            cbDrinks.alpha = 1 - 0.5f * (!cbDrinks.isClickable).toInt()
            cbSweets.alpha = 1 - 0.5f * (!cbSweets.isClickable).toInt()

            cbFood.isChecked = false
            cbDrinks.isChecked = false
            cbSweets.isChecked = false

            setCost()
        }
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