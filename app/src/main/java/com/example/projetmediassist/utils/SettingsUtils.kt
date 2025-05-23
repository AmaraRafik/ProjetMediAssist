package com.example.projetmediassist.utils

import android.content.Context

object SettingsUtils {
    fun isDoNotDisturbEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("MediAssistPrefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("do_not_disturb", false)
    }

    fun setDoNotDisturb(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("MediAssistPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("do_not_disturb", enabled).apply()
    }
    fun setCurrentMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences("MediAssistPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("current_mode", mode).apply()
    }

    fun getCurrentMode(context: Context): String {
        val prefs = context.getSharedPreferences("MediAssistPrefs", Context.MODE_PRIVATE)
        return prefs.getString("current_mode", "En consultation") ?: "En consultation"
    }

}
