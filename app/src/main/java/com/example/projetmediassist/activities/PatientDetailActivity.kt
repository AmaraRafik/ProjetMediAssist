package com.example.projetmediassist.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.ActivityPatientDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PatientDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val patientId = intent.getIntExtra("patient_id", -1)
        if (patientId == -1) {
            Toast.makeText(this, "Erreur patient introuvable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Charger le patient depuis la base
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val patient = db.patientDao().getPatientById(patientId)
            withContext(Dispatchers.Main) {
                if (patient != null) {
                    binding.fullNameText.text = patient.fullName
                    binding.patientAgeText.text = "${patient.age} ans"
                    binding.phoneText.text = patient.phone ?: "-"
                    binding.emailText.text = patient.email ?: "-"
                    binding.addressText.text = patient.address ?: "-"
                    binding.historyText.text = patient.medicalHistory ?: "Aucun"
                    binding.allergiesText.text = patient.allergies ?: "Aucune"
                } else {
                    Toast.makeText(this@PatientDetailActivity, "Patient non trouvé", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // TODO: Actions sur les boutons
        binding.appointmentButton.setOnClickListener {
            Toast.makeText(this, "À implémenter : prise de rendez-vous", Toast.LENGTH_SHORT).show()
        }
        binding.medicalHistoryButton.setOnClickListener {
            Toast.makeText(this, "À implémenter : historique médical", Toast.LENGTH_SHORT).show()
        }
    }
}
