package com.michaelgallahancs.healthrewards

import android.content.Context
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.michaelgallahancs.healthrewards.ui.theme.DietRewardsTheme
import com.michaelgallahancs.healthrewards.utilities.CountdownTimer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.time.LocalDateTime
import kotlin.math.log

class MainActivity : ComponentActivity() {
    private val Context.dataStore by preferencesDataStore("settings")

    private lateinit var btnSubMiles : Button
    private lateinit var addButton: Button
    private lateinit var milesCounterText: TextView
    private lateinit var tvCountdown : TextView

    // Initialize with time from storage
    private val timer : CountdownTimer = CountdownTimer("00:00:30")

    private object Keys {
        // save on stop/pause
        // pull on create
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
        timer.start(tvCountdown)

        // ##### Restore data from datastore #####
        lifecycleScope.launch() {
            milesCounterText.text = getMilesFromDataStore()
            val tokens = getTokensFromDataStore()
            val timerCreateTime = getTimerDateFromDataStore()
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch() {
            saveAll()
        }
    }

    override fun onStart() {
        super.onStart()

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
            settings[Keys.TOKEN_COUNT] = "0"
            Log.d("healthapp", settings[Keys.MILE_COUNT]?:"Mile count not set")
        }

        Log.d("healthapp", getMilesFromDataStore())
    }

    fun subtractMiles(view: View) {
        milesCounterText.text = ("${(milesCounterText.text.toString().toInt() - 1)}")
    }

    fun addMiles(view: View) {
        milesCounterText.text = ("${(milesCounterText.text.toString().toInt() + 1)}")
    }
}