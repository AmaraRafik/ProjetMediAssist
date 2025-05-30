package com.example.projetmediassist.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.utils.SettingsUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmartModesActivity : AppCompatActivity() {

    private lateinit var currentModeText: TextView
    private lateinit var homeVisitLayout: LinearLayout
    private lateinit var doNotDisturbLayout: LinearLayout
    private lateinit var doNotDisturbSwitch: Switch
    private lateinit var absentLayout: LinearLayout
    private lateinit var absentMessageEditText: EditText
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var configureSlotsButton: Button

    private lateinit var doctorEmail: String
    private var currentMode: String = "En consultation"
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_modes)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        doctorEmail = prefs.getString("doctorEmail", null) ?: run {
            Toast.makeText(this, getString(R.string.error_not_logged_in), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Récupération des vues
        currentModeText = findViewById(R.id.currentModeText)
        homeVisitLayout = findViewById(R.id.homeVisitLayout)
        doNotDisturbLayout = findViewById(R.id.doNotDisturbLayout)
        doNotDisturbSwitch = findViewById(R.id.doNotDisturbSwitch)
        absentLayout = findViewById(R.id.absentLayout)
        absentMessageEditText = findViewById(R.id.absentMessageEditText)
        startDateEditText = findViewById(R.id.absenceStartEditText)
        endDateEditText = findViewById(R.id.absenceEndEditText)
        configureSlotsButton = findViewById(R.id.configureSlotsButton)

        // Appliquer mode courant
        currentMode = SettingsUtils.getCurrentMode(this)
        updateCurrentMode()

        // Gestion Visite à domicile
        homeVisitLayout.setOnClickListener {
            currentMode = "Visite à domicile"
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()
            startActivity(Intent(this, HomeVisitActivity::class.java))
        }

        // Gestion Ne pas déranger
        doNotDisturbSwitch.isChecked = SettingsUtils.isDoNotDisturbEnabled(this)
        doNotDisturbSwitch.setOnCheckedChangeListener { _, isChecked ->
            SettingsUtils.setDoNotDisturb(this, isChecked)
            currentMode = if (isChecked) "Ne pas déranger" else "En consultation"
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()

            val msgRes = if (isChecked) R.string.mode_dnd_on else R.string.mode_dnd_off
            Toast.makeText(this, getString(msgRes), Toast.LENGTH_SHORT).show()
        }

        // Sélection de dates d’absence
        val openDatePicker = { editText: EditText ->
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    editText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        startDateEditText.setOnClickListener { openDatePicker(startDateEditText) }
        endDateEditText.setOnClickListener { openDatePicker(endDateEditText) }

        // Gestion absence
        configureSlotsButton.setOnClickListener {
            currentMode = "Absent"
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()

            val startStr = startDateEditText.text.toString()
            val endStr = endDateEditText.text.toString()

            if (startStr.isBlank() || endStr.isBlank()) {
                Toast.makeText(this, "Veuillez spécifier les dates de début et de fin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startMillis = dateFormat.parse(startStr)?.time ?: 0
            val endMillis = dateFormat.parse(endStr)?.time ?: 0

            if (startMillis >= endMillis) {
                Toast.makeText(this, "La date de fin doit être après la date de début.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = AppDatabase.getDatabase(this)

            lifecycleScope.launch {
                val toDelete = db.appointmentDao().getAppointmentsForDoctor(doctorEmail)
                    .filter { it.timeInMillis in startMillis..endMillis }

                toDelete.forEach { db.appointmentDao().delete(it) }

                val emails = toDelete.mapNotNull { it.patientEmail?.takeIf { mail -> mail.isNotBlank() } }.toSet()

                if (emails.isNotEmpty()) {
                    val msg = absentMessageEditText.text.toString().ifBlank {
                        "Bonjour, le docteur sera absent du $startStr au $endStr. Merci de reprogrammer votre rendez-vous."
                    }

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
                        putExtra(Intent.EXTRA_SUBJECT, "Indisponibilité du docteur")
                        putExtra(Intent.EXTRA_TEXT, msg)
                    }

                    try {
                        startActivity(Intent.createChooser(intent, "Envoyer un message"))
                    } catch (e: Exception) {
                        Toast.makeText(this@SmartModesActivity, "Aucune application email trouvée", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SmartModesActivity, "Aucun patient avec email à contacter", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun updateCurrentMode() {
        currentModeText.text = getString(R.string.current_mode_format, currentMode)

        homeVisitLayout.setBackgroundResource(R.drawable.rounded_border)
        doNotDisturbLayout.setBackgroundResource(R.drawable.rounded_border)
        absentLayout.setBackgroundResource(R.drawable.rounded_border)

        when (currentMode) {
            "Visite à domicile" -> homeVisitLayout.setBackgroundResource(R.drawable.rounded_green_background)
            "Ne pas déranger" -> doNotDisturbLayout.setBackgroundResource(R.drawable.rounded_green_background)
            "Absent" -> absentLayout.setBackgroundResource(R.drawable.rounded_green_background)
        }
    }
}
