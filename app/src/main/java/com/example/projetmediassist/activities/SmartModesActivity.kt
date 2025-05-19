package com.example.projetmediassist.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.R

class SmartModesActivity : AppCompatActivity() {
    private lateinit var currentModeText: TextView
    private lateinit var homeVisitLayout: LinearLayout
    private lateinit var doNotDisturbLayout: LinearLayout
    private lateinit var absentLayout: LinearLayout
    private lateinit var absentMessageEditText: EditText
    private lateinit var configureSlotsButton: Button

    private var currentMode: String = "En consultation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_modes)

        currentModeText = findViewById(R.id.currentModeText)
        homeVisitLayout = findViewById(R.id.homeVisitLayout)
        doNotDisturbLayout = findViewById(R.id.doNotDisturbLayout)
        absentLayout = findViewById(R.id.absentLayout)
        absentMessageEditText = findViewById(R.id.absentMessageEditText)
        configureSlotsButton = findViewById(R.id.configureSlotsButton)

        currentModeText.text = "Mode actuel : $currentMode"

        // Gestion des clics
        homeVisitLayout.setOnClickListener {
            currentMode = "Visite à domicile"
            updateCurrentMode()
        }

        doNotDisturbLayout.setOnClickListener {
            currentMode = "Ne pas déranger"
            updateCurrentMode()
        }

        absentLayout.setOnClickListener {
            currentMode = "Absent"
            updateCurrentMode()
        }

        configureSlotsButton.setOnClickListener {
            // TODO : À toi d’ajouter la logique d’ouverture des créneaux !
        }
    }

    private fun updateCurrentMode() {
        currentModeText.text = "Mode actuel : $currentMode"
        // Ajoute ici ta logique pour sauvegarder le mode ou changer l’UI si tu veux.
    }
}
