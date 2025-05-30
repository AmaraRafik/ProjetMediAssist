package com.example.projetmediassist.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.models.Patient

class PatientAdapter(
    private val onPatientClick: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = getItem(position)
        holder.nameText.text = patient.fullName
        holder.ageText.text = "Âge : ${patient.age} ans"
        holder.lastAppointmentText.text = "Dernière visite : ${patient.lastAppointment}"

        holder.itemView.setOnClickListener {
            onPatientClick(patient)
        }
    }

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.patientNameText)
        val ageText: TextView = itemView.findViewById(R.id.patientAgeText)
        val lastAppointmentText: TextView = itemView.findViewById(R.id.lastAppointmentText)
    }

    class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }
}
