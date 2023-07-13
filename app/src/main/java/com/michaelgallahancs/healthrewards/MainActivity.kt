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
import com.michaelgallahancs.healthrewards.utilities.Countdown

class MainActivity : ComponentActivity() {
    private val Context.dataStore by preferencesDataStore(name = "user data")
    private lateinit var btnSubMiles : Button
    private lateinit var addButton: Button
    private lateinit var milesCounterText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DietRewardsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }

            setContentView(R.layout.activity_main)
            btnSubMiles = findViewById(R.id.btnSubMiles) as Button
            addButton = findViewById(R.id.btnAddMiles) as Button
            milesCounterText = findViewById(R.id.tvMilesCounter) as TextView
            val timer = Countdown("48:11:45")

        }
    }

    fun subtractMiles(view: View) {
        milesCounterText.setText(((milesCounterText.text as String).toInt() - 1).toString())
        Log.d("dietapp", Countdown("11:43:42").toSeconds().toString())
    }

    fun addMiles(view: View) {
        milesCounterText.setText(((milesCounterText.text as String).toInt() + 1).toString())
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DietRewardsTheme {
        Greeting("Android")
    }
}