package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val doctorName = intent.getStringExtra("doctorName") ?: "Docteur"
        findViewById<TextView>(R.id.doctorNameText).text = doctorName
    }

    fun onAgendaClick(view: View) {

    }

    fun onPatientsClick(view: View) {
        val intent = Intent(this, PatientListActivity::class.java)
        startActivity(intent)

        }

    fun onModesClick(view: View) {

            }

    fun onSettingsClick(view: View) {

    }
}

