package com.example.projetmediassist.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.utils.LocaleUtils

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("session", MODE_PRIVATE)
        val lang = prefs.getString("lang", null)
        val context = if (lang != null) LocaleUtils.setLocale(newBase, lang) else newBase
        super.attachBaseContext(context)
    }
}
