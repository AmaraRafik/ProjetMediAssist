package com.example.projetmediassist.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.models.Ordonnance
import com.example.projetmediassist.models.OrdonnanceMedicament
import java.text.SimpleDateFormat
import java.util.*

class OrdonnanceHistoryAdapter(
    private val items: List<Pair<Ordonnance, List<OrdonnanceMedicament>>>,
    private val onItemClick: (Ordonnance, List<OrdonnanceMedicament>) -> Unit
) : RecyclerView.Adapter<OrdonnanceHistoryAdapter.OrdonnanceViewHolder>() {

    inner class OrdonnanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.ordonnanceDate)
        val diagnosticText: TextView = itemView.findViewById(R.id.ordonnanceDiagnostic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdonnanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ordonnance_history, parent, false)
        return OrdonnanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdonnanceViewHolder, position: Int) {
        val (ordonnance, _) = items[position]

        val formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            .format(Date(ordonnance.date))
        holder.dateText.text = formattedDate
        holder.diagnosticText.text = ordonnance.diagnostic

        holder.itemView.setOnClickListener {
            // On protège contre un éventuel "index out of bounds" si animations
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val (o, meds) = items[pos]
                onItemClick(o, meds)
            }
        }
    }

    override fun getItemCount() = items.size
}
