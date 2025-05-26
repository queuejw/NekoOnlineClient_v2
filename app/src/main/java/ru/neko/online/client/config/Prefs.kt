package ru.neko.online.client.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Prefs(context: Context) {

    private val prefsName: String = "game_prefs"
    private val preferences: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    var isFirstLaunch
        get() = preferences.getBoolean("isFirstLaunch", true)
        set(value) = preferences.edit { putBoolean("isFirstLaunch", value) }
}