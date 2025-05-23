package com.example.projetmediassist.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import com.example.projetmediassist.R
import com.example.projetmediassist.adapters.OrdonnanceHistoryAdapter
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.models.Ordonnance
import com.example.projetmediassist.models.OrdonnanceMedicament
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PatientMedicalHistoryActivity : AppCompatActivity() {

    private lateinit var detailOverlay: View
    private lateinit var detailCard: CardView
    private lateinit var detailDate: TextView
    private lateinit var detailPatient: TextView
    private lateinit var detailDoctor: TextView
    private lateinit var detailDiagnostic: TextView
    private lateinit var detailMeds: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_medical_history)

        val recyclerView = findViewById<RecyclerView>(R.id.ordonnanceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Overlay et carte détail
        detailOverlay = findViewById(R.id.detailOverlay)
        detailCard = findViewById(R.id.detailCard)
        detailDate = findViewById(R.id.detailDate)
        detailPatient = findViewById(R.id.detailPatient)
        detailDoctor = findViewById(R.id.detailDoctor)
        detailDiagnostic = findViewById(R.id.detailDiagnostic)
        detailMeds = findViewById(R.id.detailMeds)

        val patientName = intent.getStringExtra("patient_name") ?: ""
        findViewById<TextView>(R.id.patientNameTitle).text = patientName

        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val ordonnances = db.ordonnanceDao().getOrdonnancesByPatient(patientName)
            val ordMedDao = db.ordonnanceMedicamentDao()
            val ordonnancesWithMeds = ordonnances.map { ordonnance ->
                val meds = ordMedDao.getByOrdonnance(ordonnance.id)
                Pair(ordonnance, meds)
            }
            withContext(Dispatchers.Main) {
                recyclerView.adapter = OrdonnanceHistoryAdapter(
                    ordonnancesWithMeds
                ) { ordonnance, meds ->
                    showDetailCard(ordonnance, meds)
                }
            }
        }

        // Ferme l’overlay et la carte au clic n’importe où dessus
        detailOverlay.setOnClickListener { detailOverlay.visibility = View.GONE }
        detailCard.setOnClickListener { detailOverlay.visibility = View.GONE }
    }

    private fun showDetailCard(ordonnance: Ordonnance, meds: List<OrdonnanceMedicament>) {
        detailOverlay.visibility = View.VISIBLE

        // Format la date pour affichage humain
        val formattedDate = try {
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(ordonnance.date))
        } catch (e: Exception) {
            ordonnance.date.toString()
        }

        detailDate.text = "Date : $formattedDate"
        // Les champs sont en gras côté XML
        detailPatient.text = ordonnance.patientName
        detailDoctor.text = ordonnance.doctorName
        detailDiagnostic.text = ordonnance.diagnostic

        if (meds.isNotEmpty()) {
            val medText = meds.joinToString("\n") { "- ${it.nom} : ${it.posologie}" }
            detailMeds.visibility = View.VISIBLE
            detailMeds.text = medText
        } else {
            detailMeds.visibility = View.GONE
        }
    }
}
