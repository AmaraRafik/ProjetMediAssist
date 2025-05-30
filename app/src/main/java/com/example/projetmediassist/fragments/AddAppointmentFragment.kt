package com.example.projetmediassist.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.FragmentAddAppointmentBinding
import com.example.projetmediassist.models.Appointment
import com.example.projetmediassist.utils.NotificationUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

        val blue = ContextCompat.getColor(requireContext(), R.color.medical_blue)
        val gray = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        val tint = android.content.res.ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(blue, gray)
        )
        binding.optionCabinet.buttonTintList = tint
        binding.optionDomicile.buttonTintList = tint

        val prefillPatientName = arguments?.getString("prefill_patient_name")
        val prefillPatientEmail = arguments?.getString("prefill_patient_email")

        binding.patientEditText.setText(prefillPatientName ?: "")
        binding.patientEmailEditText.setText(prefillPatientEmail ?: "")

        val prefs = requireActivity().getSharedPreferences("session", 0)
        val doctorEmail = prefs.getString("doctorEmail", null) ?: return

        val db = AppDatabase.getDatabase(requireContext())
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val selectedDayMillis = calendar.timeInMillis

        lifecycleScope.launch {
            val takenHours = db.appointmentDao()
                .getAppointmentsForDoctorOnDate(doctorEmail, selectedDayMillis)
                .map { it.hour }

            val availableSlots = generateTimeSlots("09:00", "15:00", 30)
                .filter { it !in takenHours }

            if (availableSlots.isEmpty()) {
                Toast.makeText(requireContext(), "Aucun créneau disponible ce jour-là", Toast.LENGTH_LONG).show()
                dismiss()
                return@launch
            }

            val slotAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                availableSlots
            )
            binding.hourAutoComplete.setAdapter(slotAdapter)
            binding.hourAutoComplete.setOnClickListener {
                binding.hourAutoComplete.showDropDown()
            }
        }

        binding.saveAppointmentButton.setOnClickListener {
            val patientName = binding.patientEditText.text.toString().trim()
            val hour = binding.hourAutoComplete.text.toString().trim()
            val email = binding.patientEmailEditText.text.toString().trim()
            val description = when {
                binding.optionCabinet.isChecked -> "cabinet"
                binding.optionDomicile.isChecked -> "domicile"
                else -> ""
            }

            if (patientName.isEmpty() || hour.isEmpty() || description.isEmpty()) {
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

            lifecycleScope.launch {
                val patient = db.patientDao().getPatientByName(patientName)
                val patientId = patient?.id ?: -1
                val displayName = patient?.fullName ?: patientName

                val appointment = Appointment(
                    doctorEmail = doctorEmail,
                    patient = displayName,
                    patientId = patientId,
                    patientEmail = email.ifEmpty { null },
                    hour = hour,
                    description = description,
                    date = selectedDate,
                    timeInMillis = timeInMillis
                )

                db.appointmentDao().insert(appointment)

                NotificationUtils.scheduleAppointmentNotifications(
                    requireContext(), timeInMillis, displayName
                )

                listener?.onAppointmentAdded()
                Toast.makeText(requireContext(), "Rendez-vous ajouté", Toast.LENGTH_SHORT).show()

                if (patient == null) {
                    showCustomPatientDialog(patientName, email)
                } else {
                    dismiss()
                }
            }
        }
    }

    private fun showCustomPatientDialog(patientName: String, email: String) {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val view = layoutInflater.inflate(R.layout.custom_dialog_layout, null)

        val yesButton = view.findViewById<Button>(R.id.yesButton)
        val noButton = view.findViewById<Button>(R.id.noButton)

        yesButton.setOnClickListener {
            val addPatientFragment = AddPatientFragment.newInstance(patientName, email)
            addPatientFragment.listener = object : OnPatientAddedListener {
                override fun onPatientAdded() {
                    Toast.makeText(requireContext(), "Patient ajouté", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
            addPatientFragment.show(parentFragmentManager, "AddPatient")
            dialog.dismiss()
        }

        noButton.setOnClickListener {
            dialog.dismiss()
            dismiss()
        }

        dialog.setView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.75).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateTimeSlots(start: String, end: String, intervalMinutes: Int): List<String> {
        val sdf = SimpleDateFormat("HH:mm", Locale.FRANCE)
        val startTime = Calendar.getInstance().apply { time = sdf.parse(start)!! }
        val endTime = Calendar.getInstance().apply { time = sdf.parse(end)!! }

        val slots = mutableListOf<String>()
        while (startTime <= endTime) {
            slots.add(sdf.format(startTime.time))
            startTime.add(Calendar.MINUTE, intervalMinutes)
        }

        return slots
    }
}
