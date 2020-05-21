package com.asiantech.intern2019winterfinal.utils

import android.content.Context

class SharedPrefs(context: Context) {
    companion object {
        private const val PREFS_NAME = "huyheo"
        const val KEY_BILL = "BillInfo"
        const val KEY_USER_LOGIN = "UserLogin"
        const val KEY_LIST_TABLE_SELECTED = "ListTableSelected"
    }

    private val pref by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // trong modun mới gọi dc
    internal fun getString(key: String): String {
        return pref.getString(key, "").toString()
    }

    internal fun getInt(key: String): Int {
        return pref.getInt(key, 0)
    }

    internal fun putString(key: String, value: String) {
        pref.edit().putString(key, value).apply()
    }

    internal fun putInt(key: String, value: Int) {
        pref.edit().putInt(key, value).apply()
    }

    internal fun clear() {
    }

    internal fun clearAll() {
        pref.edit().clear().apply()
    }
}
