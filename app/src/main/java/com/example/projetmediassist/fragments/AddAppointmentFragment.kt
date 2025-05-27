package com.example.projetmediassist.fragments

import android.app.AlertDialog
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
import com.example.projetmediassist.utils.NotificationUtils
import kotlinx.coroutines.launch
import java.util.*
import android.util.Log
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.example.projetmediassist.R


interface OnAppointmentAddedListener {
    fun onAppointmentAdded()
}

class AddAppointmentFragment : DialogFragment() {

    private var _binding: FragmentAddAppointmentBinding? = null
    private val binding get() = _binding!!

    var listener: OnAppointmentAddedListener? = null
    var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Teinte bleue pour boutons radio
        val blue = ContextCompat.getColor(requireContext(), R.color.medical_blue)
        val gray = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

        val tint = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(blue, gray)
        )

        binding.optionCabinet.buttonTintList = tint
        binding.optionDomicile.buttonTintList = tint

        val prefillPatientName = arguments?.getString("prefill_patient_name")
        if (!prefillPatientName.isNullOrBlank()) {
            binding.patientEditText.setText(prefillPatientName)
        }

        val prefs = requireActivity().getSharedPreferences("session", 0)
        val doctorEmail = prefs.getString("doctorEmail", null)

        binding.saveAppointmentButton.setOnClickListener {
            val patientName = binding.patientEditText.text.toString().trim()
            val hour = binding.hourEditText.text.toString().trim()
            val email = binding.patientEmailEditText.text.toString().trim()
            val description = when {
                binding.optionCabinet.isChecked -> "cabinet"
                binding.optionDomicile.isChecked -> "domicile"
                else -> ""
            }

            if (patientName.isEmpty() || hour.isEmpty() || description.isEmpty() || doctorEmail == null) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

            val db = AppDatabase.getDatabase(requireContext())

            lifecycleScope.launch {
                val patient = db.patientDao().getPatientByName(patientName)
                val patientId = patient?.id ?: -1
                val displayName = patient?.fullName ?: patientName

                // Alerte si patient inconnu (facultative)
                if (patient == null) {
                    Toast.makeText(requireContext(), "⚠️ Patient non enregistré. RDV quand même créé.", Toast.LENGTH_SHORT).show()

                    AlertDialog.Builder(requireContext())
                        .setTitle("Patient non trouvé")
                        .setMessage("Souhaitez-vous enregistrer ce patient dans votre base ?")
                        .setPositiveButton("Oui") { _, _ ->
                            val addPatientFragment = AddPatientFragment.newInstance(patientName, email)
                            addPatientFragment.show(parentFragmentManager, "AddPatient")
                        }
                        .setNegativeButton("Non", null)
                        .show()
                }

                val appointment = Appointment(
                    doctorEmail = doctorEmail,
                    patient = displayName,
                    patientId = patientId,
                    patientEmail = if (email.isNotEmpty()) email else null,
                    hour = hour,
                    description = description,
                    date = selectedDate,
                    timeInMillis = timeInMillis
                )

                db.appointmentDao().insert(appointment)

                NotificationUtils.scheduleAppointmentNotifications(
                    context = requireContext(),
                    appointmentTime = timeInMillis,
                    patientName = displayName
                )

                Log.d("NOTIF", "✅ RDV enregistré et notifications programmées")
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
