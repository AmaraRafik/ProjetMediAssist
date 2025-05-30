package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var doctorEmail: String
    private lateinit var doctorName: String // Declare doctorName here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        doctorEmail = prefs.getString("doctorEmail", null) ?: run {
            // Handle case where doctorEmail is not found, e.g., redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        // Retrieve doctorName from SharedPreferences
        doctorName = prefs.getString("doctorName", "Docteur") ?: "Docteur" // Get doctor's name from prefs

        // Afficher le nom du médecin
        findViewById<TextView>(R.id.doctorNameText).text = "Dr. $doctorName" // Use the retrieved doctorName

        // Charger le prochain RDV
        loadNextAppointment()

        // Navigation
        findViewById<LinearLayout>(R.id.agendaCard).setOnClickListener {
            startActivity(Intent(this, AgendaActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.patientsCard).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.modesCard).setOnClickListener {
            startActivity(Intent(this, SmartModesActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.settingsCard).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // 🔵 Recharge le prochain RDV dès qu’on revient sur le dashboard !
        loadNextAppointment()
        // Also update doctor name in case it was changed in settings (though your settings doesn't allow it currently)
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        doctorName = prefs.getString("doctorName", "Docteur") ?: "Docteur"
        findViewById<TextView>(R.id.doctorNameText).text = "Dr. $doctorName"
    }

    private fun loadNextAppointment() {
        val db = AppDatabase.getDatabase(this)
        val now = System.currentTimeMillis()

        lifecycleScope.launch {
            val next = db.appointmentDao().getNextAppointment(doctorEmail, now)

            withContext(Dispatchers.Main) {
                val rdvDateView = findViewById<TextView>(R.id.rdvDate)
                val rdvPatientView = findViewById<TextView>(R.id.rdvPatient)

                if (next != null) {
                    val format = SimpleDateFormat("EEE. dd MMMM à HH:mm", Locale("fr"))
                    val formattedDate = format.format(Date(next.timeInMillis)).replaceFirstChar { it.uppercase() }
                    rdvDateView.text = formattedDate
                    rdvPatientView.text = next.patient
                } else {
                    rdvDateView.text = "Aucun RDV à venir"
                    rdvPatientView.text = ""
                }
            }
        }
    }
}