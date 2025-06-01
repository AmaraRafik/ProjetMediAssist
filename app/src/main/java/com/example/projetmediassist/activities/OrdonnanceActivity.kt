package com.example.projetmediassist.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.adapters.MedicamentAdapter
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.models.Medicament
import com.example.projetmediassist.models.Ordonnance
import com.example.projetmediassist.models.OrdonnanceMedicament
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdonnanceActivity : BaseActivity() {

    private lateinit var adapter: MedicamentAdapter
    private lateinit var medicaments: MutableList<Medicament>
    private lateinit var globalAlertCard: androidx.cardview.widget.CardView
    private lateinit var globalAlertText: TextView

    // Infos patient et docteur
    private var allergies: List<String> = emptyList()
    private var antecedents: List<String> = emptyList()
    private var diagnostic: String = ""
    private var patientName: String = ""
    private var patientEmail: String = ""
    private var doctorName: String = ""
    private var symptomes: String = ""

    // E-mail de la pharmacie défini en dur
    private val PHARMACY_EMAIL = "pharmacie.test@example.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordonnance)

        diagnostic = intent.getStringExtra("diagnostic") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: ""
        symptomes = intent.getStringExtra("symptomes") ?: ""

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        doctorName = prefs.getString("doctorName", "") ?: ""

        findViewById<TextView>(R.id.patientNameText).text = patientName
        findViewById<TextView>(R.id.diagnosticText).text = diagnostic

        globalAlertCard = findViewById(R.id.globalAlertCard)
        globalAlertText = findViewById(R.id.globalAlertText)

        val recyclerView = findViewById<RecyclerView>(R.id.medicamentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val addMedicamentBtn = findViewById<MaterialButton>(R.id.addMedicamentBtn)
        val saveOrdonnanceBtn = findViewById<MaterialButton>(R.id.saveOrdonnanceBtn)

        // Récupérer le bouton "Envoyer au patient / pharmacie"
        val envoyerBtn = findViewById<MaterialButton>(R.id.envoyerBtn)

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val patient = withContext(Dispatchers.IO) {
                db.patientDao().getPatientByFullName(patientName)
            }

            // Récupérer l'e-mail du patient
            patientEmail = patient?.email ?: ""

            allergies = patient?.allergies
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

            antecedents = patient?.medicalHistory
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

            val medicamentNoms = withContext(Dispatchers.IO) {
                db.associationMaladieMedicamentDao().getMedicamentsForMaladie(diagnostic)
            }
            medicaments = withContext(Dispatchers.IO) {
                db.medicamentDao().getByNoms(medicamentNoms)
            }.toMutableList()

            adapter = MedicamentAdapter(medicaments) {
                Toast.makeText(this@OrdonnanceActivity, getString(R.string.ordonnance_toast_med_deleted), Toast.LENGTH_SHORT).show()
                checkGlobalWarnings()
            }
            recyclerView.adapter = adapter
            checkGlobalWarnings()
        }

        addMedicamentBtn.setOnClickListener {
            showAddMedicamentDialog()
        }

        saveOrdonnanceBtn.setOnClickListener {
            saveOrdonnanceInDatabase()
        }

        // Liaison de bouton "Envoyer au patient / pharmacie" à la fonction d'envoi d'e-mail
        envoyerBtn.setOnClickListener {
            sendPrescriptionByEmail()
        }
    }

    private fun showAddMedicamentDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_medicament, null)
        val inputNom = dialogView.findViewById<EditText>(R.id.inputMedName)
        val inputPosologie = dialogView.findViewById<EditText>(R.id.inputMedPosologie)
        val saveButton = dialogView.findViewById<Button>(R.id.saveMedicamentButton)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val nom = inputNom.text.toString().trim()
            val posologie = inputPosologie.text.toString().trim()
            if (nom.isBlank()) {
                Toast.makeText(this, getString(R.string.ordonnance_dialog_nom_required), Toast.LENGTH_SHORT).show()
            } else {
                val nouveauMedicament = Medicament(
                    id = 0,
                    nom = nom,
                    description = "",
                    contreIndications = "",
                    interactions = "",
                    posologie = posologie
                )
                adapter.addMedicament(nouveauMedicament)
                checkGlobalWarnings()
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    private fun saveOrdonnanceInDatabase() {
        if (medicaments.isEmpty()) {
            Toast.makeText(this, getString(R.string.ordonnance_toast_add_one_med), Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val ordonnance = Ordonnance(
                patientName = patientName,
                doctorName = doctorName,
                diagnostic = diagnostic,
                date = System.currentTimeMillis()
            )
            val ordonnanceId = withContext(Dispatchers.IO) {
                db.ordonnanceDao().insert(ordonnance)
            }

            val ordMedList = medicaments.map {
                OrdonnanceMedicament(
                    ordonnanceId = ordonnanceId.toInt(),
                    nom = it.nom,
                    posologie = it.posologie ?: ""
                )
            }
            withContext(Dispatchers.IO) {
                db.ordonnanceMedicamentDao().insertAll(ordMedList)
            }

            runOnUiThread {
                Toast.makeText(this@OrdonnanceActivity, getString(R.string.ordonnance_saved), Toast.LENGTH_LONG).show()
                val intent = Intent(this@OrdonnanceActivity, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun checkGlobalWarnings() {
        val allergyList = mutableListOf<String>()
        val antecedentList = mutableListOf<String>()
        val interactionPairs = mutableListOf<Pair<String, String>>()

        for (i in medicaments.indices) {
            val med = medicaments[i]
            // Allergies
            val isAllergic = allergies.any { allergie ->
                med.contreIndications?.contains(allergie, ignoreCase = true) == true
            }
            if (isAllergic) allergyList.add(med.nom)

            // Antécédents
            val isContra = antecedents.any { ant ->
                med.contreIndications?.contains(ant, ignoreCase = true) == true
            }
            if (isContra) antecedentList.add(med.nom)

            // Incompatibilité médicamenteuse
            val interactions = med.interactions
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

            for (j in medicaments.indices) {
                if (i != j) {
                    val med2 = medicaments[j]
                    if (interactions.contains(med2.nom.trim().lowercase())) {
                        val pair = if (med.nom < med2.nom) med.nom to med2.nom else med2.nom to med.nom
                        if (!interactionPairs.contains(pair)) interactionPairs.add(pair)
                    }
                }
            }
        }

        val messages = mutableListOf<String>()
        if (allergyList.isNotEmpty())
            messages.add(getString(R.string.ordonnance_allergy_detected) + ":\n${allergyList.joinToString("\n") { "• $it" }}")
        if (antecedentList.isNotEmpty())
            messages.add(getString(R.string.ordonnance_antecedent_risk) + ":\n${antecedentList.joinToString("\n") { "• $it" }}")
        if (interactionPairs.isNotEmpty())
            messages.add(getString(R.string.ordonnance_incompatible_meds) + ":\n" + interactionPairs.joinToString("\n") { "• ${it.first} ↔ ${it.second}" })

        if (messages.isNotEmpty()) {
            globalAlertCard.visibility = android.view.View.VISIBLE
            globalAlertText.text = messages.joinToString("\n\n")
        } else {
            globalAlertCard.visibility = android.view.View.GONE
        }
    }

    private fun sendPrescriptionByEmail() {
        // 1. Vérifier si l'e-mail du patient est disponible
        if (patientEmail.isBlank()) {
            Toast.makeText(this, getString(R.string.ordonnance_email_missing), Toast.LENGTH_LONG).show()
            return
        }
        if (medicaments.isEmpty()) {
            Toast.makeText(this, getString(R.string.ordonnance_add_at_least_one_med), Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Construire le contenu de l'ordonnance textuel
        val ordonnanceText = StringBuilder()
        ordonnanceText.append("Ordonnance pour le patient : ${patientName}\n")
        ordonnanceText.append("Diagnostic : ${diagnostic}\n")
        ordonnanceText.append("Dr. : ${doctorName}\n")
        ordonnanceText.append("\n--- Médicaments ---\n")
        if (medicaments.isNotEmpty()) {
            medicaments.forEachIndexed { index, medicament ->
                ordonnanceText.append("${index + 1}. ${medicament.nom}")
                if (!medicament.posologie.isNullOrBlank()) {
                    ordonnanceText.append(" - Posologie: ${medicament.posologie}")
                }
                ordonnanceText.append("\n")
            }
        } else {
            ordonnanceText.append("Aucun médicament prescrit.\n")
        }
        ordonnanceText.append("--------------------\n")
        if (symptomes.isNotBlank()) {
            ordonnanceText.append("Symptômes observés: ${symptomes}\n")
        }

        val emailSubject = getString(R.string.ordonnance_email_subject, doctorName)
        val emailBody = "Bonjour ${patientName},\n\n" +
                "Voici votre ordonnance émise par le Dr. ${doctorName}.\n\n" +
                ordonnanceText.toString() +
                "\n\nMerci de la présenter à votre pharmacien.\n\n" +
                "Cordialement,\n" +
                "Dr. ${doctorName}"

        // 3. Préparer les destinataires
        val recipients = mutableListOf<String>()
        recipients.add(patientEmail) // Le patient
        recipients.add(PHARMACY_EMAIL) // La pharmacie définie en dur

        // 4. Créer l'Intent d'e-mail
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Permet de s'assurer que seules les applications d'e-mail gèrent cela

            putExtra(Intent.EXTRA_EMAIL, recipients.toTypedArray()) // Les destinataires
            putExtra(Intent.EXTRA_SUBJECT, emailSubject)           // Le sujet
            putExtra(Intent.EXTRA_TEXT, emailBody)                 // Le corps du texte
        }

        // 5. Lancer l'application d'e-mail
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.ordonnance_email_send_via)))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.ordonnance_no_email_app), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
