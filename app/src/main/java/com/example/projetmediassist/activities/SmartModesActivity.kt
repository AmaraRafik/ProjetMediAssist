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
    private var currentMode: String = "En consultation" // Initialize with a default
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
            // Toggle logic for "Visite à domicile"
            if (currentMode == "Visite à domicile") {
                currentMode = "En consultation" // Deactivate home visit
            } else {
                currentMode = "Visite à domicile" // Activate home visit
                // If HomeVisitActivity needs to be launched every time it's *activated*,
                // you can keep this line here. If it's just for initial setup, move it.
                startActivity(Intent(this, HomeVisitActivity::class.java))
            }
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()
            Toast.makeText(this, "Mode: $currentMode", Toast.LENGTH_SHORT).show()
        }


        // Gestion Ne pas déranger
        // Ensure the switch state matches the current mode on load
        doNotDisturbSwitch.isChecked = (currentMode == "Ne pas déranger")
        doNotDisturbSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentMode = "Ne pas déranger"
            } else {
                currentMode = "En consultation" // Default mode when DND is off
            }
            SettingsUtils.setDoNotDisturb(this, isChecked) // Keep this for DND specific setting
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
            // Only set mode to Absent if dates are valid and action is taken
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

            currentMode = "Absent" // Set mode to Absent only when action is initiated
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()


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
                    Toast.makeText(this@SmartModesActivity, "Aucun patient avec email à contacter ou aucun rendez-vous à supprimer pour cette période.", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun updateCurrentMode() {
        currentModeText.text = getString(R.string.current_mode_format, currentMode)

        // Reset all backgrounds to default (white border) first
        homeVisitLayout.setBackgroundResource(R.drawable.rounded_border)
        doNotDisturbLayout.setBackgroundResource(R.drawable.rounded_border)
        absentLayout.setBackgroundResource(R.drawable.rounded_border)

        // Then apply green background to the active mode
        when (currentMode) {
            "Visite à domicile" -> homeVisitLayout.setBackgroundResource(R.drawable.rounded_green_background)
            "Ne pas déranger" -> doNotDisturbLayout.setBackgroundResource(R.drawable.rounded_green_background)
            "Absent" -> absentLayout.setBackgroundResource(R.drawable.rounded_green_background)
        }

        // Make sure the Do Not Disturb switch state reflects the current mode
        // Only set the switch state if the current mode is explicitly "Ne pas déranger"
        // or if it's "En consultation" AND doNotDisturb was previously enabled (which means it's now off)
        if (currentMode == "Ne pas déranger") {
            doNotDisturbSwitch.isChecked = true
        } else {
            doNotDisturbSwitch.isChecked = false
        }
    }
}