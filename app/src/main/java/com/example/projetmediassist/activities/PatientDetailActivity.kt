package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.ActivityPatientDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PatientDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityPatientDetailBinding
    private var patientName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val patientId = intent.getIntExtra("patient_id", -1)
        if (patientId == -1) {
            Toast.makeText(this, getString(R.string.patient_detail_error_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Charger le patient depuis la base
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val patient = db.patientDao().getPatientById(patientId)
            withContext(Dispatchers.Main) {
                if (patient != null) {
                    patientName = patient.fullName
                    binding.fullNameText.text = patient.fullName
                    binding.patientAgeText.text = getString(R.string.patient_detail_age_format, patient.age)
                    binding.phoneText.text = patient.phone ?: "-"
                    binding.emailText.text = patient.email ?: "-"
                    binding.addressText.text = patient.address ?: "-"
                    binding.historyText.text = patient.medicalHistory ?: getString(R.string.patient_detail_history_none)
                    binding.allergiesText.text = patient.allergies ?: getString(R.string.patient_detail_allergies_none)
                } else {
                    Toast.makeText(this@PatientDetailActivity, getString(R.string.patient_detail_error_not_found2), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // Quand on clique sur "Créer un rendez-vous"
        binding.appointmentButton.setOnClickListener {
            // On passe le nom du patient à AgendaActivity
            val intent = Intent(this, AgendaActivity::class.java)
            intent.putExtra("prefill_patient_name", patientName)
            intent.putExtra("prefill_patient_email", binding.emailText.text.toString())
            startActivity(intent)
        }

        binding.medicalHistoryButton.setOnClickListener {
            val intent = Intent(this, PatientMedicalHistoryActivity::class.java)
            intent.putExtra("patient_name", patientName) // ou patient_id si tu préfères
            startActivity(intent)
        }

    }
}
