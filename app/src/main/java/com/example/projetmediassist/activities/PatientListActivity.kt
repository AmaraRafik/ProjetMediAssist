package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetmediassist.adapters.PatientAdapter
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.ActivityPatientListBinding
import com.example.projetmediassist.fragments.AddPatientFragment
import com.example.projetmediassist.fragments.OnPatientAddedListener
import com.example.projetmediassist.models.Patient
import kotlinx.coroutines.launch

class PatientListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientListBinding
    private lateinit var adapter: PatientAdapter
    private var doctorEmail: String? = null
    private var fullPatientList: List<Patient> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PatientAdapter { patient ->
            val intent = Intent(this, PatientDetailActivity::class.java)
            intent.putExtra("patient_id", patient.id)
            startActivity(intent)
        }

        binding.patientRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.patientRecyclerView.adapter = adapter

        val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
        doctorEmail = sharedPref.getString("doctorEmail", null)

        binding.addPatientButton.setOnClickListener {
            val fragment = AddPatientFragment()
            fragment.listener = object : OnPatientAddedListener {
                override fun onPatientAdded() {
                    refreshPatients()
                }
            }
            fragment.show(supportFragmentManager, "AddPatientFragment")
        }

        setupSearchListener()
    }

    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filteredList = if (query.isEmpty()) {
                    fullPatientList
                } else {
                    fullPatientList.filter {
                        it.fullName.contains(query, ignoreCase = true)
                    }
                }
                adapter.submitList(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun refreshPatients() {
        if (doctorEmail != null) {
            val db = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                fullPatientList = db.patientDao().getPatientsForDoctor(doctorEmail!!)
                adapter.submitList(fullPatientList)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPatients()
    }
}
