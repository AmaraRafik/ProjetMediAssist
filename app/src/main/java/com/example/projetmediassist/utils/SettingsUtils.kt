package com.example.projetmediassist.utils

import android.content.Context

object SettingsUtils {

    private const val PREF_NAME = "MediAssistPrefs"

    fun isDoNotDisturbEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("do_not_disturb", false)
    }

    fun setDoNotDisturb(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("do_not_disturb", enabled).apply()
    }

    fun setCurrentMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("current_mode", mode).apply()
    }

    fun getCurrentMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString("current_mode", "En consultation") ?: "En consultation"
    }

    fun setAbsencePeriod(context: Context, startMillis: Long, endMillis: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("absence_start", startMillis)
            .putLong("absence_end", endMillis)
            .apply()
    }

    fun getAbsenceStart(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("absence_start", 0L)
    }

    fun getAbsenceEnd(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("absence_end", 0L)
    }
}
