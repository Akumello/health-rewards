package com.michaelgallahancs.healthrewards.data

import android.content.Context
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit

class Settings {
    // Keys
    val timerStoreKey = stringPreferencesKey("timerCreationDateTime")

    suspend fun saveTimerSettings() {

    }
}