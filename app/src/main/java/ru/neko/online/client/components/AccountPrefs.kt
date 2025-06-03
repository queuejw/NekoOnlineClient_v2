package ru.neko.online.client.components

import android.content.Context
import androidx.core.content.edit

class AccountPrefs(context: Context) {

    private val prefsName: String = "account_prefs"
    private val catsPrefsName: String = "account_cats_prefs"

    private val accountPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val accountCatsPrefs = context.getSharedPreferences(catsPrefsName, Context.MODE_PRIVATE)

    var accountPassword
        get() = accountPrefs.getString("account_password", "null")
        set(value) = accountPrefs.edit { putString("account_password", value) }

    var accountName
        get() = accountPrefs.getString("account_name", "null")
        set(value) = accountPrefs.edit { putString("account_name", value) }

    var userToken
        get() = accountPrefs.getString("user_token", "null")
        set(value) = accountPrefs.edit { putString("user_token", value) }

    var userId
        get() = accountPrefs.getLong("user_id", -1)
        set(value) = accountPrefs.edit { putLong("user_id", value) }

    var accountUsername
        get() = accountPrefs.getString("account_username", "null")
        set(value) = accountPrefs.edit { putString("account_username", value) }

    var serverAddress
        get() = accountPrefs.getString("server_address", "127.0.0.1")
        set(value) = accountPrefs.edit { putString("server_address", value) }

    var serverPort
        get() = accountPrefs.getInt("server_port", 1011)
        set(value) = accountPrefs.edit { putInt("server_port", value) }

    var userDataName
        get() = accountPrefs.getString("userdata_name", "null")
        set(value) = accountPrefs.edit { putString("userdata_name", value) }

    var userDataNCoins
        get() = accountPrefs.getInt("userdata_ncoins", 0)
        set(value) = accountPrefs.edit { putInt("userdata_ncoins", value) }

    var userDataFood
        get() = accountPrefs.getInt("userdata_food", 0)
        set(value) = accountPrefs.edit { putInt("userdata_food", value) }

    var userDataWater
        get() = accountPrefs.getInt("userdata_water", 0)
        set(value) = accountPrefs.edit { putInt("userdata_water", value) }

    var userDataToys
        get() = accountPrefs.getInt("userdata_toys", 0)
        set(value) = accountPrefs.edit { putInt("userdata_toys", value) }


    fun clearServerConfig(): Boolean {
        accountPrefs.edit {
            remove("server_port")
            remove("server_address")
        }
        return true
    }

    fun clearUserData(): Boolean {
        accountPrefs.edit {
            remove("user_token")
            remove("user_id")
            remove("account_password")
            remove("account_name")
            remove("userdata_name")
            remove("userdata_ncoins")
            remove("userdata_food")
            remove("userdata_water")
            remove("userdata_toys")
        }
        return true
    }

}