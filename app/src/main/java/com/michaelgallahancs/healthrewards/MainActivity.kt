package com.michaelgallahancs.healthrewards

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.michaelgallahancs.healthrewards.utilities.CountdownTimer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime

//TODO: Disable spend button when cost is not enough
// TODO:

class MainActivity : ComponentActivity() {
    private val Context.dataStore by preferencesDataStore("settings")

    private val goal : Double = 6.5
    private val startingTime : String = "48:00:00"
    private val timerCheckPoints : Array<String> = arrayOf(startingTime, "24:00:00", "12:00:00")
    private var drinkStreak : Int = 0
    private val hourOfNewDay : Int = 4 // e.g. 4 for 4am or 20 for 8pm
    private val centAmountWater : Int = 5
    private val centAmountKeto : Int = 10
    private val centAmountFast : Int = 20
    private val centAmountCare : Int = 5
    private val centAmountFloss : Int = 5

    private lateinit var btnSubMiles : Button
    private lateinit var addButton: Button
    private lateinit var btnSpend : Button
    private lateinit var milesCounterText: TextView
    private lateinit var tvCountdown : TextView
    private lateinit var tvTokenCount : TextView
    private lateinit var linLayoutBackground : LinearLayout
    private lateinit var cbFood : CheckBox
    private lateinit var cbDrinks : CheckBox
    private lateinit var cbSweets : CheckBox
    private lateinit var tvCost : TextView
    private lateinit var tvRules : TextView
    private lateinit var tvCentCount : TextView
    private lateinit var switchFast : Switch
    private lateinit var switchKeto : Switch
    private lateinit var switchCare : Switch
    private lateinit var switchFloss : Switch
    private lateinit var switchWater : Switch

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
        val KETO_STATE = stringPreferencesKey("keto_state")
        val WATER_STATE = stringPreferencesKey("water_state")
        val CARE_STATE = stringPreferencesKey("care_state")
        val FLOSS_STATE = stringPreferencesKey("floss_state")
        val FAST_STATE = stringPreferencesKey("fast_state")
        val CENT_COUNT = stringPreferencesKey("cent_state")
    }

    private fun log(output : String){
        Log.d("healthapp", output)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSubMiles = findViewById<Button>(R.id.btnSubMiles)
        addButton = findViewById<Button>(R.id.btnAddMiles)
        btnSpend = findViewById<Button>(R.id.btnSpend)
        milesCounterText = findViewById<TextView>(R.id.tvMilesCounter)
        tvCountdown = findViewById<TextView>(R.id.tvCountdown)
        tvTokenCount = findViewById<TextView>(R.id.tvTokenCount)
        linLayoutBackground = findViewById<LinearLayout>(R.id.linLayoutBackground)
        cbFood = findViewById<CheckBox>(R.id.cbFood)
        cbDrinks = findViewById<CheckBox>(R.id.cbDrinks)
        cbSweets = findViewById<CheckBox>(R.id.cbSweets)
        tvCost = findViewById<TextView>(R.id.tvCost)
        tvRules = findViewById<TextView>(R.id.tvRules)
        tvRules.text = "How it works:\n\n\u2022 Reach the $goal mile goal to earn a token\n\n\u2022 Spend tokens on rewards\n\n\u2022 Lose one token when the countdown timer ends\n\n\u2022 Reset the timer by earning a token"
        tvCentCount = findViewById<TextView>(R.id.tvCentCount)
        switchFast = findViewById<Switch>(R.id.switchFast)
        switchKeto = findViewById<Switch>(R.id.switchKeto)
        switchCare = findViewById<Switch>(R.id.switchCare)
        switchFloss = findViewById<Switch>(R.id.switchFloss)
        switchWater = findViewById<Switch>(R.id.switchWater)

        // ##### Restore data from datastore #####
        lifecycleScope.launch() {
            val savedFast : String = getStringFromDataStore(Keys.FAST_STATE)
            val savedWater : String = getStringFromDataStore(Keys.WATER_STATE)
            val savedCare : String = getStringFromDataStore(Keys.CARE_STATE)
            val savedKeto : String = getStringFromDataStore(Keys.KETO_STATE)
            val savedFloss : String = getStringFromDataStore(Keys.FLOSS_STATE)
            var savedDayLastOpened : String = getStringFromDataStore(Keys.LAST_DAY_OF_YEAR)
            val savedFood : String = getStringFromDataStore(Keys.FOOD_STATE)
            val savedDrinks : String = getStringFromDataStore(Keys.DRINKS_STATE)
            val savedSweets : String = getStringFromDataStore(Keys.SWEETS_STATE)
            val savedTokenCnt : String = getStringFromDataStore(Keys.TOKEN_COUNT)
            val savedCentCnt : String = getStringFromDataStore(Keys.CENT_COUNT)
            val savedMiles : String = getStringFromDataStore(Keys.MILE_COUNT)
            val savedTimer : String = getStringFromDataStore(Keys.TIMER_CREATED)

            val waterChecked : Int = if(savedWater != "") savedWater.toInt() else 0
            val careChecked : Int = if(savedCare != "") savedCare.toInt() else 0
            val flossChecked : Int = if(savedFloss != "") savedFloss.toInt() else 0
            val ketoChecked : Int = if(savedKeto != "") savedKeto.toInt() else 0
            val fastChecked : Int = if(savedFast != "") savedFast.toInt() else 0
            val foodChecked : Int = if(savedFood != "") savedFood.toInt() else 0
            val drinksChecked : Int = if(savedDrinks != "") savedDrinks.toInt() else 0
            val sweetsChecked : Int = if(savedSweets != "") savedSweets.toInt() else 0
            val tokenCount : Int = if (savedTokenCnt != "") savedTokenCnt.toInt() else 0
            val milesCount : Double = if (savedMiles != "") savedMiles.toDouble() else 0.0
            val centCount : Int = if (savedCentCnt != "") savedCentCnt.toInt() else 0
            val dayLastOpened : Int = if (savedDayLastOpened != "") savedDayLastOpened.toInt() else 0

            milesCounterText.text = milesCount.toString()
            tvTokenCount.text = tokenCount.toString()
            tvCentCount.text = centCount.toString()
            btnSubMiles.isEnabled = (milesCount > 0.0) // Subtract inactive when 0
            switchWater.isChecked = waterChecked == 1
            switchCare.isChecked = careChecked == 1
            switchFloss.isChecked = flossChecked == 1
            switchKeto.isChecked = ketoChecked == 1
            switchFast.isChecked = fastChecked == 1

            // TODO modify date comparison to work an the hour of the users choosing
            if (LocalDateTime.now().dayOfYear == dayLastOpened) {
                cbFood.isClickable = foodChecked == 1
                cbDrinks.isClickable = drinksChecked == 1
                cbSweets.isClickable = sweetsChecked == 1

                cbFood.alpha = if (cbFood.isClickable) 1.0f else 0.5f
                cbDrinks.alpha = if (cbDrinks.isClickable) 1.0f else 0.5f
                cbSweets.alpha = if (cbSweets.isClickable) 1.0f else 0.5f

                setCost()
            } else {
                addCents()
                switchFast.isActivated = false
                switchCare.isActivated = false
                switchFloss.isActivated = false
                switchKeto.isActivated = false
                switchWater.isActivated = false
            }

            timer = if (savedTimer != "")
                CountdownTimer(
                    startingTime,
                    LocalDateTime.parse(savedTimer),
                    timerCheckpointCallback
                )
            else
                CountdownTimer(startingTime, timerCheckpointCallback)

            timer.setCheckpoints(*timerCheckPoints)
            timer.start(tvCountdown)
        }
    }

    override fun onStop() {
        super.onStop()
        log("Saving... ${switchWater.isChecked.toInt()}")
        lifecycleScope.launch() {
            saveAll()
        }
    }

    private val timerCheckpointCallback : (String) -> Unit = {
        //Log.d("healthapp", "callback has been executed: $it | match: ${it.matches("[0-9]+".toRegex())}")
        val tokenTotal = tvTokenCount.text.toString().toInt()

        // Subtract from tokens the number of timers consumed since the app was opened last
        if (it.matches("[0-9]+".toRegex())) {
            if (it.toInt() <= tokenTotal)
                tvTokenCount.text = "${tokenTotal - it.toString().toInt()}"
            else
                tvTokenCount.text = "${0}"
        }

        when (it) {
            timerCheckPoints[0] -> tvCountdown.setBackgroundColor(0x6B4CAF42) // Green
            timerCheckPoints[1] -> tvCountdown.setBackgroundColor(0x4DFFEB3B) // Yellow
            timerCheckPoints[2] -> tvCountdown.setBackgroundColor(0x4DF44336) // Red
            "00:00:00" -> {
                if (tokenTotal > 0)
                    tvTokenCount.text = "${tokenTotal - 1}"

                timer.restart()
            }
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
            settings[Keys.FAST_STATE] = switchFast.isChecked.toInt().toString()
            settings[Keys.WATER_STATE] = switchWater.isChecked.toInt().toString()
            settings[Keys.CARE_STATE] = switchCare.isChecked.toInt().toString()
            settings[Keys.KETO_STATE] = switchKeto.isChecked.toInt().toString()
            settings[Keys.FLOSS_STATE] = switchFloss.isChecked.toInt().toString()
            settings[Keys.CENT_COUNT] = tvCentCount.text.toString()
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
        btnSpend.isEnabled = cost > 0 && cost <= tvTokenCount.text.toString().toDouble()
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

            if(cbDrinks.isChecked) {
                drinkStreak += 1
                // TODO set today's date as most recent drink spend
            }

            cbFood.isChecked = false
            cbDrinks.isChecked = false
            cbSweets.isChecked = false

            setCost()
        }
    }

    private fun addCents() {
        val curCentCount : Int = tvCentCount.text.toString().toInt()
        var cents : Int = 0
        cents += if(switchWater.isChecked) centAmountWater else 0
        cents += if(switchCare.isChecked) centAmountCare else 0
        cents += if(switchFloss.isChecked) centAmountFloss else 0
        cents += if(switchKeto.isChecked) centAmountKeto else 0
        cents += if(switchFast.isChecked) centAmountFast else 0

        tvCentCount.text = (curCentCount + cents).toString()
    }

    fun subtractMiles(view: View) {
        var result = milesCounterText.text.toString().toDouble() - 0.5
        if (result >= 0)
            milesCounterText.text = ("${(milesCounterText.text.toString().toDouble() - 0.5)}")
        if (result <= 0.0)
            btnSubMiles.isEnabled = false
    }

    fun addMiles(view: View) {
        var result = milesCounterText.text.toString().toDouble() + 0.5

        if (result == goal) {
            result = 0.0
            tvTokenCount.text = "${tvTokenCount.text.toString().toInt() + 1}"
            timer.restart()
        }

        btnSubMiles.isEnabled = result > 0.0
        milesCounterText.text = "$result"
    }
}