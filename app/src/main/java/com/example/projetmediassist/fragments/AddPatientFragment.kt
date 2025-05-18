package com.example.projetmediassist.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.FragmentAddPatientBinding
import com.example.projetmediassist.models.Patient
import kotlinx.coroutines.launch

// Interface de rappel pour informer l’activité hôte
interface OnPatientAddedListener {
    fun onPatientAdded()
}

class AddPatientFragment : DialogFragment() {

    private var _binding: FragmentAddPatientBinding? = null
    private val binding get() = _binding!!

    // Le callback que l’activité peut définir
    var listener: OnPatientAddedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("session", 0)
        val doctorEmail = prefs.getString("doctorEmail", null)

        binding.savePatientButton.setOnClickListener {
            val name = binding.fullNameEditText.text.toString().trim()
            val age = binding.ageEditText.text.toString().toIntOrNull() ?: 0
            val lastVisit = binding.lastAppointmentEditText.text.toString().trim()

            if (name.isEmpty() || age <= 0 || lastVisit.isEmpty() || doctorEmail == null) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPatient = Patient(
                fullName = name,
                age = age,
                lastAppointment = lastVisit,
                doctorEmail = doctorEmail
            )

            val db = AppDatabase.getDatabase(requireContext())

            lifecycleScope.launch {
                db.patientDao().insert(newPatient)
                dismiss() // ferme le fragment

                // Callback vers l’activité
                listener?.onPatientAdded()

                Toast.makeText(requireContext(), "Patient ajouté !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
