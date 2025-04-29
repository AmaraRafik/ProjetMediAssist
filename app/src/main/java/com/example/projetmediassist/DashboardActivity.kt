package com.example.projetmediassist

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Example: You can set the doctor’s name dynamically later
        val doctorNameText = findViewById<TextView>(R.id.doctorNameText)
        doctorNameText.text = "Dr. Dupont"
    }

    fun onAgendaClick(view: View) {
        // TODO: startActivity(Intent(this, AgendaActivity::class.java))
    }

    fun onPatientsClick(view: View) {
        // TODO: startActivity(Intent(this, PatientsActivity::class.java))
    }

    fun onModesClick(view: View) {
        // TODO: startActivity(Intent(this, ModesActivity::class.java))
    }

    fun onSettingsClick(view: View) {
        // TODO: startActivity(Intent(this, SettingsActivity::class.java))
    }
}
