package com.example.projetmediassist.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ConsultationActivity : BaseActivity() {

    private var isRecording = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultation)

        val patientName = intent.getStringExtra("patient_name") ?: getString(R.string.consultation_patient_placeholder)
        findViewById<TextView>(R.id.patientNameText).text = patientName

        val microButton = findViewById<ImageButton>(R.id.microButton)
        val speechTextEditText = findViewById<EditText>(R.id.speechTextEditText)
        val detectedEditText = findViewById<EditText>(R.id.detectedSymptomsEditText)
        val diagnosticEditText = findViewById<EditText>(R.id.diagnosticText)

        // --- Micro : reconnaissance vocale remplit le texte patient ET détecte les keywords ---
        val speechLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.getOrNull(0) ?: ""
                speechTextEditText.setText(text)
                detectKeywordsAndFillDetectedEditText(text, detectedEditText)
            }
            // Toujours remettre le micro en bleu à la fin de la reconnaissance, même si annulée
            microButton.setColorFilter(ContextCompat.getColor(this, R.color.medical_blue))
            isRecording = false
        }

        microButton.setOnClickListener {
            if (!isRecording) {
                isRecording = true
                microButton.setColorFilter(Color.parseColor("#16C672"))
                Toast.makeText(this, getString(R.string.consultation_toast_recording_started), Toast.LENGTH_SHORT).show()
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.consultation_speech_prompt))
                try {
                    speechLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.consultation_voice_not_supported), Toast.LENGTH_SHORT).show()
                    isRecording = false
                    microButton.setColorFilter(ContextCompat.getColor(this, R.color.medical_blue))
                }
            }
        }

        // Diagnostic MAJ uniquement quand symptômes détectés changent
        detectedEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateDiagnosticFromKeywords(s.toString(), diagnosticEditText)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // === GESTION DU PASSAGE À L'ORDONNANCE ===
        val generatePrescriptionBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.generatePrescriptionBtn)
        generatePrescriptionBtn.setOnClickListener {
            val diagnostic = diagnosticEditText.text.toString()
            val patientNameText = findViewById<TextView>(R.id.patientNameText).text.toString()

            if (diagnostic.isBlank() || diagnostic == getString(R.string.consultation_no_diagnostic)) {
                Toast.makeText(this, getString(R.string.consultation_toast_generate_diagnostic_first), Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, OrdonnanceActivity::class.java)
                intent.putExtra("diagnostic", diagnostic)
                intent.putExtra("patient_name", patientNameText)
                startActivity(intent)
            }
        }
    }

    private fun detectKeywordsAndFillDetectedEditText(speechText: String, detectedEditText: EditText) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ConsultationActivity)
            val allSymptomes = withContext(Dispatchers.IO) { db.symptomeDao().getAll() }
            val tokens = speechText.lowercase(Locale.ROOT)
                .split(",", " ", ";", ".", "–", "-", "\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()

            val found = mutableSetOf<String>()
            for (symptome in allSymptomes) {
                val allNames = mutableListOf<String>()
                allNames.add(symptome.nom.lowercase(Locale.ROOT))
                if (symptome.synonymes.isNotBlank())
                    allNames.addAll(symptome.synonymes.split(",").map { it.trim().lowercase(Locale.ROOT) })
                if (tokens.any { t -> allNames.contains(t) }) {
                    found.add(symptome.nom)
                }
            }
            // Affiche les keywords détectés séparés par des virgules, EDITABLE
            withContext(Dispatchers.Main) {
                detectedEditText.setText(found.joinToString(", "))
            }
        }
    }

    private fun updateDiagnosticFromKeywords(keywords: String, diagnosticEditText: EditText) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ConsultationActivity)
            val allSymptomes = withContext(Dispatchers.IO) { db.symptomeDao().getAll() }
            val tokens = keywords
                .lowercase(Locale.ROOT)
                .split(",", " ", ";", ".", "–", "-", "\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()

            val matchedSymptomeIds = mutableSetOf<Int>()
            for (symptome in allSymptomes) {
                if (tokens.contains(symptome.nom.lowercase(Locale.ROOT))) {
                    matchedSymptomeIds.add(symptome.id)
                }
            }

            val associations = withContext(Dispatchers.IO) { db.associationSymptomeMaladieDao().getAll() }
            val maladieIdToCount = mutableMapOf<Int, Int>()
            for (assoc in associations) {
                if (matchedSymptomeIds.contains(assoc.symptomeId)) {
                    maladieIdToCount[assoc.maladieId] = maladieIdToCount.getOrDefault(assoc.maladieId, 0) + 1
                }
            }
            val maladieId = maladieIdToCount.maxByOrNull { it.value }?.key
            val maladie = if (maladieId != null) withContext(Dispatchers.IO) { db.maladieDao().getMaladieById(maladieId) } else null

            withContext(Dispatchers.Main) {
                diagnosticEditText.setText(
                    maladie?.nom ?: getString(R.string.consultation_no_diagnostic)
                )
            }
        }
    }
}
