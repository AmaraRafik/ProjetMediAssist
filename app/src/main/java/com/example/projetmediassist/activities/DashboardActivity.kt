package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val doctorName = intent.getStringExtra("doctorName") ?: "Docteur"
        findViewById<TextView>(R.id.doctorNameText).text = doctorName

        val agendaCard = findViewById<LinearLayout>(R.id.agendaCard)
        val patientsCard = findViewById<LinearLayout>(R.id.patientsCard)
        val modesCard = findViewById<LinearLayout>(R.id.modesCard)
        val settingsCard = findViewById<LinearLayout>(R.id.settingsCard)

        agendaCard.setOnClickListener {
            startActivity(Intent(this, AgendaActivity::class.java))
        }

        patientsCard.setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java)
            startActivity(intent)
        }


        modesCard.setOnClickListener {
            // À adapter avec ton activité réelle
            Toast.makeText(this, "Modes intelligents à venir", Toast.LENGTH_SHORT).show()
        }

        //val settingsCard = findViewById<LinearLayout>(R.id.settingsCard)

        settingsCard.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

    }

    
}

