package com.example.projetmediassist.fragments

import android.app.Dialog
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

// AJOUTE CETTE INTERFACE ICI (en dehors de la classe)
interface OnPatientAddedListener {
    fun onPatientAdded()
}

class AddPatientFragment : DialogFragment() {
    private var _binding: FragmentAddPatientBinding? = null
    private val binding get() = _binding!!

    // Le listener pour callback
    var onPatientAddedListener: OnPatientAddedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Récupère l'email du médecin connecté
        val doctorEmail = requireActivity().getSharedPreferences("MediAssistPrefs", 0)
            .getString("doctorEmail", null)

        binding.savePatientButton.setOnClickListener {
            val fullName = binding.fullNameEditText.text.toString()
            val age = binding.ageEditText.text.toString().toIntOrNull() ?: 0
            val lastAppointment = binding.lastAppointmentEditText.text.toString()

            if (fullName.isBlank() || age == 0 || lastAppointment.isBlank() || doctorEmail == null) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val patient = Patient(
                fullName = fullName,
                age = age,
                lastAppointment = lastAppointment,
                doctorEmail = doctorEmail
            )

            // Ajout en base
            val db = AppDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                db.patientDao().insert(patient)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Patient ajouté !", Toast.LENGTH_SHORT).show()
                    // Appelle le callback pour rafraîchir la liste !
                    onPatientAddedListener?.onPatientAdded()
                    dismiss() // Ferme le fragment
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
