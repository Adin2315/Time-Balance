package com.adindaapriliawahyupp_231111015.timebalance.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "TimeBalancePref"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // Simpan status login dan user ID
    fun setLoggedIn(isLoggedIn: Boolean, userId: Int) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
    }

    // Cek apakah user sudah login
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Ambil user ID yang sedang login
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }

    // Logout: hapus semua data session
    fun logout() {
        editor.clear()
        editor.apply()
    }
}
