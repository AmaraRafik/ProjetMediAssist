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
import com.example.projetmediassist.databinding.FragmentAddAppointmentBinding
import com.example.projetmediassist.models.Appointment
import kotlinx.coroutines.launch
import java.util.*

interface OnAppointmentAddedListener {
    fun onAppointmentAdded()
}

class AddAppointmentFragment : DialogFragment() {

    private var _binding: FragmentAddAppointmentBinding? = null
    private val binding get() = _binding!!

    var listener: OnAppointmentAddedListener? = null
    var selectedDate: Long = System.currentTimeMillis()

    // ---- AJOUT pour rendre l'arrière-plan transparent ----
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
    // -----------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Prérenseigne le nom du patient si présent dans arguments (depuis la fiche patient)
        val prefillPatientName = arguments?.getString("prefill_patient_name")
        if (!prefillPatientName.isNullOrBlank()) {
            binding.patientEditText.setText(prefillPatientName)
            // Optionnel : rendre non modifiable
            // binding.patientEditText.isEnabled = false
        }

        val prefs = requireActivity().getSharedPreferences("session", 0)
        val doctorEmail = prefs.getString("doctorEmail", null)

        binding.saveAppointmentButton.setOnClickListener {
            val patient = binding.patientEditText.text.toString().trim()
            val hour = binding.hourEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()

            if (patient.isEmpty() || hour.isEmpty() || description.isEmpty() || doctorEmail == null) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convertir heure en timeInMillis
            val cal = Calendar.getInstance()
            cal.timeInMillis = selectedDate

            val parts = hour.split(":")
            val hourInt = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minInt = parts.getOrNull(1)?.toIntOrNull() ?: 0

            cal.set(Calendar.HOUR_OF_DAY, hourInt)
            cal.set(Calendar.MINUTE, minInt)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val timeInMillis = cal.timeInMillis

            val appointment = Appointment(
                doctorEmail = doctorEmail,
                patient = patient,
                hour = hour,
                description = description,
                date = selectedDate,
                timeInMillis = timeInMillis // utilisé pour le tri
            )

            val db = AppDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                db.appointmentDao().insert(appointment)
                listener?.onAppointmentAdded()
                dismiss()
                Toast.makeText(requireContext(), "Rendez-vous ajouté", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
