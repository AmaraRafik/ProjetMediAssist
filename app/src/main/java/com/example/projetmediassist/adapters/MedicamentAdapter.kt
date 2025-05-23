package com.example.projetmediassist.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.models.Medicament

class MedicamentAdapter(
    private val medicaments: MutableList<Medicament>,
    private val onMedicamentDeleted: ((Medicament) -> Unit)? = null
) : RecyclerView.Adapter<MedicamentAdapter.MedicamentViewHolder>() {

    inner class MedicamentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medName: TextView = itemView.findViewById(R.id.medName)
        val medPosologie: EditText = itemView.findViewById(R.id.medPosologie)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        var currentTextWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicament, parent, false)
        return MedicamentViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicamentViewHolder, position: Int) {
        val medicament = medicaments[position]
        holder.medName.text = medicament.nom

        // Évite le bug du TextWatcher multiple !
        holder.currentTextWatcher?.let {
            holder.medPosologie.removeTextChangedListener(it)
        }
        holder.medPosologie.setText(medicament.posologie ?: "")
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                medicament.posologie = s?.toString() ?: ""
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.medPosologie.addTextChangedListener(watcher)
        holder.currentTextWatcher = watcher

        // Suppression
        holder.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val removed = medicaments.removeAt(pos)
                notifyDataSetChanged()
                onMedicamentDeleted?.invoke(removed)
            }
        }


    }

    override fun getItemCount(): Int = medicaments.size

    fun addMedicament(medicament: Medicament) {
        medicaments.add(medicament)
        notifyDataSetChanged()
    }
}
