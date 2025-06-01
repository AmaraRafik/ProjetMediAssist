package com.example.projetmediassist.utils

import android.content.Context
import android.os.Build
import java.util.*

object LocaleUtils {
    fun setLocale(context: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)

            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            config.setLayoutDirection(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            return context
        }
    }
}



