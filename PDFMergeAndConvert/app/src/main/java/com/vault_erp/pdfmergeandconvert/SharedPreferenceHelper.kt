package com.vault_erp.pdfmergeandconvert

import android.content.Context
import android.content.SharedPreferences


class SharedPreferenceHelper(ctx: Context, private val DBName: String) {
    private var PRIVATE_MODE = 0

    private val sharedPref: SharedPreferences = ctx.getSharedPreferences(DBName, PRIVATE_MODE)

    fun save(key: String, value: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPref.getBoolean(key, defaultValue)
    }

    fun getBool(key: String, defaultValue: Boolean = false): Boolean? {
        return sharedPref.getBoolean(key, defaultValue)
    }

    fun save(key: String, value: String) {
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String {
        return sharedPref.getString(key, "").orEmpty()
    }

    fun save(key: String, value: Int) {
        val editor = sharedPref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String): Int {
        return sharedPref.getInt(key, -1)
    }

    fun deleteSharedPref() {
        sharedPref.edit().clear().apply()
    }
}