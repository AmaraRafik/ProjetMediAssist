package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Récupérer l'email ou le nom depuis SharedPreferences
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val doctorName = prefs.getString("doctorName", "Médecin") ?: "Médecin"

        val doctorNameText = findViewById<TextView>(R.id.doctorNameText)
        doctorNameText.text = "Dr. $doctorName"

        val logoutBtn = findViewById<TextView>(R.id.logoutButton)
        logoutBtn.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val syncButton = findViewById<Button>(R.id.syncButton)
        syncButton.setOnClickListener {
            Toast.makeText(this, "Fonction non encore disponible", Toast.LENGTH_SHORT).show()
        }

        val languageLayout = findViewById<LinearLayout>(R.id.languageLayout)
        languageLayout.setOnClickListener {
            Toast.makeText(this, "Changement de langue à venir", Toast.LENGTH_SHORT).show()
        }

        val notificationSwitch = findViewById<Switch>(R.id.notificationSwitch)
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Notifications activées" else "Notifications désactivées", Toast.LENGTH_SHORT).show()
        }
    }
}
