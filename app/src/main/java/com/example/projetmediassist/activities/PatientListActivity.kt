package com.example.projetmediassist.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetmediassist.adapters.PatientAdapter
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.ActivityPatientListBinding
import com.example.projetmediassist.models.Patient
import kotlinx.coroutines.launch

class PatientListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientListBinding
    private lateinit var adapter: PatientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prépare l’adapter
        adapter = PatientAdapter()
        binding.patientRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.patientRecyclerView.adapter = adapter

        // Récupère l’email du médecin connecté (depuis la session)
        //val sharedPref = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
        //val doctorEmail = sharedPref.getString("doctorEmail", null)

        //if (doctorEmail != null) {
        //    val db = AppDatabase.getDatabase(this)
        //    lifecycleScope.launch {
        //        val patients = db.patientDao().getPatientsForDoctor(doctorEmail)
        //        adapter.submitList(patients)
        //    }
        //}

        // Ajout du patient (à faire plus tard)
        val fakePatients = listOf(
            Patient(
                id = 1,
                fullName = "Rafik Lemalade",
                age = 41,
                lastAppointment = "12 septembre 2024",
                doctorEmail = "toto@med.com"
            ),
            Patient(
                id = 2,
                fullName = "Elhadi Lefou",
                age = 67,
                lastAppointment = "12 septembre 2024",
                doctorEmail = "toto@med.com"
            ),
            Patient(
                id = 3,
                fullName = "Issa Lebrb",
                age = 1001,
                lastAppointment = "30 juin 2090",
                doctorEmail = "toto@med.com"
            )
        )
        adapter.submitList(fakePatients)
        binding.addPatientButton.setOnClickListener {
            // TODO: lancer l’activité d’ajout de patient
        }

        // Recherche : à faire plus tard aussi !
        // binding.searchEditText.addTextChangedListener { ... }
    }
}
