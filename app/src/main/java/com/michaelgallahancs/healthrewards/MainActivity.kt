package com.michaelgallahancs.healthrewards

import android.content.Context
import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.preferencesDataStore
import com.michaelgallahancs.healthrewards.ui.theme.DietRewardsTheme
import com.michaelgallahancs.healthrewards.utilities.CountdownTimer

class MainActivity : ComponentActivity() {
    private val Context.dataStore by preferencesDataStore(name = "user data")
    private lateinit var btnSubMiles : Button
    private lateinit var addButton: Button
    private lateinit var milesCounterText: TextView
    private val timer : CountdownTimer = CountdownTimer("00:01:00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            setContentView(R.layout.activity_main)
            btnSubMiles = findViewById<Button>(R.id.btnSubMiles)
            addButton = findViewById<Button>(R.id.btnAddMiles)
            milesCounterText = findViewById<TextView>(R.id.tvMilesCounter)
        }
    }

    fun subtractMiles(view: View) {
        milesCounterText.text = ("${(milesCounterText.text.toString().toInt() - 1)}")
    }

    fun addMiles(view: View) {
        milesCounterText.text = ("${(milesCounterText.text.toString().toInt() + 1)}")
    }
}