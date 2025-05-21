package com.example.projetmediassist.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.models.Appointment

class AppointmentAdapter(
    private val items: List<Appointment>,
    private val onDelete: (Appointment) -> Unit,
    private val onItemClick: (Appointment) -> Unit // <-- AJOUTÉ
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hourText: TextView = view.findViewById(R.id.hourText)
        val patientText: TextView = view.findViewById(R.id.patientText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = items[position]
        holder.hourText.text = appointment.hour
        holder.patientText.text = appointment.patient
        holder.descriptionText.text = appointment.description

        // Clic court : ouvrir détails
        holder.itemView.setOnClickListener {
            onItemClick(appointment)
        }

        // Clic long : suppression
        holder.itemView.setOnLongClickListener {
            onDelete(appointment)
            true
        }
    }

    override fun getItemCount() = items.size
}
