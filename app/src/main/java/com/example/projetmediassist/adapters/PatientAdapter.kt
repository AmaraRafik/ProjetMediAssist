package com.example.projetmediassist.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.models.Patient

class PatientAdapter : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {
    private var patients: List<Patient> = emptyList()

    fun submitList(newPatients: List<Patient>) {
        patients = newPatients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun getItemCount(): Int = patients.size

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]
        holder.nameText.text = patient.fullName
        holder.ageText.text = "Âge : ${patient.age} ans"
        holder.lastAppointmentText.text = "Dernière visite : ${patient.lastAppointment}"
    }

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.patientNameText)
        val ageText: TextView = itemView.findViewById(R.id.patientAgeText)
        val lastAppointmentText: TextView = itemView.findViewById(R.id.lastAppointmentText)
    }
}
