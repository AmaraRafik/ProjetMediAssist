package com.example.projetmediassist.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.models.HomeVisitItem

class HomeVisitAdapter(
    private val items: List<HomeVisitItem>,
    private val onDetailClick: (HomeVisitItem) -> Unit
) : RecyclerView.Adapter<HomeVisitAdapter.HomeVisitViewHolder>() {

    inner class HomeVisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.visit_patient_name)
        val hour: TextView = view.findViewById(R.id.visit_hour)
        val address: TextView = view.findViewById(R.id.visit_address)
        val detailText: TextView = view.findViewById(R.id.visit_detail_text)   // TextView ici !
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeVisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_visit, parent, false)
        return HomeVisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeVisitViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.patientName
        holder.hour.text = "Heure : ${item.hour}"
        holder.address.text = "Adresse : ${item.address}"
        holder.detailText.setOnClickListener { onDetailClick(item) }
    }

    override fun getItemCount() = items.size
}
