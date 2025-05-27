package com.example.projetmediassist.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.R
import android.content.Intent
import com.example.projetmediassist.utils.SettingsUtils
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.database.AppDatabase
import kotlinx.coroutines.launch

class SmartModesActivity : AppCompatActivity() {
    private lateinit var currentModeText: TextView
    private lateinit var homeVisitLayout: LinearLayout
    private lateinit var doNotDisturbLayout: LinearLayout
    private lateinit var absentLayout: LinearLayout
    private lateinit var absentMessageEditText: EditText
    private lateinit var configureSlotsButton: Button
    private lateinit var doctorEmail: String

    private var currentMode: String = "En consultation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_modes)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        doctorEmail = prefs.getString("doctorEmail", null) ?: run {
            Toast.makeText(this, "Erreur : médecin non connecté", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentModeText = findViewById(R.id.currentModeText)
        homeVisitLayout = findViewById(R.id.homeVisitLayout)
        doNotDisturbLayout = findViewById(R.id.doNotDisturbLayout)
        absentLayout = findViewById(R.id.absentLayout)
        absentMessageEditText = findViewById(R.id.absentMessageEditText)
        configureSlotsButton = findViewById(R.id.configureSlotsButton)

        // Récupère le dernier mode utilisé
        currentMode = SettingsUtils.getCurrentMode(this)
        updateCurrentMode()

        // Gestion des clics
        homeVisitLayout.setOnClickListener {
            currentMode = "Visite à domicile"
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()
            val intent = Intent(this, HomeVisitActivity::class.java)
            startActivity(intent)
        }

        doNotDisturbLayout.setOnClickListener {
            val isEnabled = SettingsUtils.isDoNotDisturbEnabled(this)
            val newState = !isEnabled
            SettingsUtils.setDoNotDisturb(this, newState)

            currentMode = if (newState) "Ne pas déranger" else "En consultation"
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()

            val message = if (newState) "🔕 Mode Ne pas déranger activé" else "🔔 Notifications réactivées"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        absentLayout.setOnClickListener {
            if (currentMode == "Absent") {
                currentMode = "En consultation"
                SettingsUtils.setCurrentMode(this, currentMode)
                updateCurrentMode()
            }
        }

        configureSlotsButton.setOnClickListener {
            currentMode = "Absent"
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()

            val db = AppDatabase.getDatabase(this)
            val nowMillis = System.currentTimeMillis()

            lifecycleScope.launch {
                val appointments = db.appointmentDao().getUpcomingAppointments(doctorEmail, nowMillis)
                val emails = mutableSetOf<String>() // ✅ utiliser Set pour éviter les doublons

                for (appt in appointments) {
                    if (!appt.patientEmail.isNullOrBlank()) {
                        emails.add(appt.patientEmail)
                    } else {
                        val patient = db.patientDao().getPatientById(appt.patientId)
                        if (patient != null && !patient.email.isNullOrBlank()) {
                            emails.add(patient.email)
                        }
                    }
                }

                if (emails.isNotEmpty()) {
                    val message = absentMessageEditText.text.toString().ifBlank {
                        "Bonjour, le docteur est momentanément absent. Merci de reprogrammer votre rendez-vous."
                    }

                    val emailIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
                        putExtra(Intent.EXTRA_SUBJECT, "Indisponibilité du docteur")
                        putExtra(Intent.EXTRA_TEXT, message)
                    }

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Envoyer un message à vos patients"))
                    } catch (e: Exception) {
                        Toast.makeText(this@SmartModesActivity, "Aucune application email trouvée", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SmartModesActivity, "Aucun patient avec email pour les rendez-vous à venir", Toast.LENGTH_SHORT).show()
                }

                startActivity(Intent(this@SmartModesActivity, AgendaActivity::class.java))
            }
        }

    }

    private fun updateCurrentMode() {
        currentModeText.text = "Mode actuel : $currentMode"

        homeVisitLayout.setBackgroundResource(R.drawable.rounded_border)
        doNotDisturbLayout.setBackgroundResource(R.drawable.rounded_border)
        absentLayout.setBackgroundResource(R.drawable.rounded_border)

        when (currentMode) {
            "Visite à domicile" -> homeVisitLayout.setBackgroundResource(R.drawable.rounded_green_background)
            "Ne pas déranger" -> doNotDisturbLayout.setBackgroundResource(R.drawable.rounded_green_background)
            "Absent" -> absentLayout.setBackgroundResource(R.drawable.rounded_green_background)
        }
    }
}
