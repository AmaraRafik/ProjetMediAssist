package com.example.projetmediassist

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val doctorName = intent.getStringExtra("doctorName") ?: "Docteur"
        findViewById<TextView>(R.id.doctorNameText).text = doctorName
    }

    fun onAgendaClick(view: View) {
        // TODO: Naviguer vers l'agenda
    }

    fun onPatientsClick(view: View) {
        // TODO: Naviguer vers les patients
    }

    fun onModesClick(view: View) {
        // TODO: Naviguer vers les modes intelligents
    }

    fun onSettingsClick(view: View) {
        // TODO: Naviguer vers les paramètres
    }
}

