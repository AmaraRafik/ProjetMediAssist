package com.example.projetmediassist.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projetmediassist.R
import com.google.android.material.button.MaterialButton

class ConsultationActivity : AppCompatActivity() {

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation)

        val patientName = intent.getStringExtra("patient_name") ?: "Nom du patient"
        findViewById<TextView>(R.id.patientNameText).text = patientName

        val microButton = findViewById<ImageButton>(R.id.microButton)
        val symptomsEditText = findViewById<EditText>(R.id.symptomsEditText)

        // Gestion du bouton micro
        microButton.setOnClickListener {
            isRecording = !isRecording
            if (isRecording) {
                // Commence l'enregistrement (visuel)
                microButton.setColorFilter(Color.parseColor("#16C672")) // Vert
                Toast.makeText(this, "Enregistrement démarré...", Toast.LENGTH_SHORT).show()
                // TODO: lancer la vraie écoute micro ici si besoin
            } else {
                // Arrête l'enregistrement (visuel)
                microButton.setColorFilter(ContextCompat.getColor(this, R.color.medical_blue))
                Toast.makeText(this, "Enregistrement arrêté.", Toast.LENGTH_SHORT).show()
                // TODO: arrêter l'écoute micro
            }
        }

        findViewById<MaterialButton>(R.id.generatePrescriptionBtn).setOnClickListener {
            Toast.makeText(this, "Génération d'ordonnance à venir...", Toast.LENGTH_SHORT).show()
            // TODO: Logique future pour ouvrir ordonnance
        }
    }
}
