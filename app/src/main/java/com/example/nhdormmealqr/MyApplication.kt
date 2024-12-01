package com.example.nhdormmealqr

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class MyApplication : Application() {
    companion object {
        // Singleton for DataStore
        private val Context.dataStore by preferencesDataStore(name = "loginInfo")
        private lateinit var instance: MyApplication

        val dataStore: androidx.datastore.core.DataStore<Preferences>
            get() = instance.dataStore
    }

    override fun onCreate() {
        super.onCreate()
        instance = this // Initialize the instance of MyApplication
    }
}