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

// Interface de rappel pour informer l’activité hôte
interface OnPatientAddedListener {
    fun onPatientAdded()
}

class AddPatientFragment : DialogFragment() {

    private var _binding: FragmentAddPatientBinding? = null
    private val binding get() = _binding!!

    // Le callback que l’activité peut définir
    var listener: OnPatientAddedListener? = null

    companion object {
        fun newInstance(fullName: String?, email: String? = null): AddPatientFragment {
            val fragment = AddPatientFragment()
            val args = Bundle()
            args.putString("full_name", fullName)
            args.putString("email", email)
            fragment.arguments = args
            return fragment
        }
    }

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
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("full_name")?.let {
            binding.fullNameEditText.setText(it)
        }
        arguments?.getString("email")?.let {
            binding.emailEditText.setText(it)
        }

        // Pour d'autres champs à l'avenir, faire de même avec d'autres clés (phone, email...)

        val prefs = requireActivity().getSharedPreferences("session", 0)
        val doctorEmail = prefs.getString("doctorEmail", null)

        binding.savePatientButton.setOnClickListener {
            val name = binding.fullNameEditText.text.toString().trim()
            val age = binding.ageEditText.text.toString().toIntOrNull() ?: 0
            val phone = binding.phoneEditText.text?.toString()?.trim()
            val email = binding.emailEditText.text?.toString()?.trim()
            val address = binding.addressEditText.text?.toString()?.trim()
            val history = binding.historyEditText.text?.toString()?.trim()
            val allergy = binding.allergyEditText.text?.toString()?.trim()

            if (name.isEmpty() || age <= 0 || doctorEmail == null) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPatient = Patient(
                fullName = name,
                age = age,
                phone = phone,
                email = email,
                address = address,
                medicalHistory = history,
                allergies = allergy,
                lastAppointment = "Aucune",
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
