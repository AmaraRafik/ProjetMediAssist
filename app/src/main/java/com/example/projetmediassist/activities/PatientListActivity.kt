package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetmediassist.adapters.PatientAdapter
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.ActivityPatientListBinding
import com.example.projetmediassist.fragments.AddPatientFragment
import com.example.projetmediassist.fragments.OnPatientAddedListener
import kotlinx.coroutines.launch

class PatientListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientListBinding
    private lateinit var adapter: PatientAdapter
    private var doctorEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔵 Adapter CLIQUABLE !
        adapter = PatientAdapter { patient ->
            val intent = Intent(this, PatientDetailActivity::class.java)
            intent.putExtra("patient_id", patient.id)
            startActivity(intent)
        }
        binding.patientRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.patientRecyclerView.adapter = adapter

        // Récupère l’email du médecin connecté (depuis la session)
        val sharedPref = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
        doctorEmail = sharedPref.getString("doctorEmail", null)

        // Ouvre le fragment au clic
        binding.addPatientButton.setOnClickListener {
            val fragment = AddPatientFragment()
            fragment.listener = object : OnPatientAddedListener {
                override fun onPatientAdded() {
                    refreshPatients()
                }
            }
            fragment.show(supportFragmentManager, "AddPatientFragment")
        }
    }

    private fun refreshPatients() {
        if (doctorEmail != null) {
            val db = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                val patients = db.patientDao().getPatientsForDoctor(doctorEmail!!)
                adapter.submitList(patients)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPatients()
    }
}
