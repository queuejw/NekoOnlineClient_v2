package ru.neko.online.client.components

import android.content.Context
import androidx.core.content.edit

class AccountPrefs(context: Context) {

    private val prefsName: String = "account_prefs"

    private val accountPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    var accountPassword
        get() = accountPrefs.getString("account_password", "null")
        set(value) = accountPrefs.edit { putString("account_password", value) }

    var accountUsername
        get() = accountPrefs.getString("account_username", "null")
        set(value) = accountPrefs.edit { putString("account_username", value) }

}