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

class SmartModesActivity : BaseActivity() {

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
    private var currentMode: String = ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_modes)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        doctorEmail = prefs.getString("doctorEmail", null) ?: run {
            Toast.makeText(this, getString(R.string.smart_modes_error_not_logged_in), Toast.LENGTH_SHORT).show()
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

        // Appliquer mode courant (par défaut "En consultation")
        currentMode = SettingsUtils.getCurrentMode(this)
        if (currentMode.isBlank()) {
            currentMode = getString(R.string.smart_modes_current_mode_default)
        }
        updateCurrentMode()

        // Gestion Visite à domicile
        homeVisitLayout.setOnClickListener {
            val homeVisit = getString(R.string.smart_modes_home_visit)
            if (currentMode == homeVisit) {
                currentMode = getString(R.string.smart_modes_current_mode_default)
            } else {
                currentMode = homeVisit
                startActivity(Intent(this, HomeVisitActivity::class.java))
            }
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()
            Toast.makeText(this, getString(R.string.smart_modes_current_mode_label, currentMode), Toast.LENGTH_SHORT).show()
        }

        // Gestion Ne pas déranger
        doNotDisturbSwitch.isChecked = (currentMode == getString(R.string.smart_modes_do_not_disturb))
        doNotDisturbSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentMode = getString(R.string.smart_modes_do_not_disturb)
            } else {
                currentMode = getString(R.string.smart_modes_current_mode_default)
            }
            SettingsUtils.setDoNotDisturb(this, isChecked)
            SettingsUtils.setCurrentMode(this, currentMode)
            updateCurrentMode()

            val msgRes = if (isChecked) R.string.smart_modes_mode_dnd_on else R.string.smart_modes_mode_dnd_off
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
            val startStr = startDateEditText.text.toString()
            val endStr = endDateEditText.text.toString()

            if (startStr.isBlank() || endStr.isBlank()) {
                Toast.makeText(this, getString(R.string.smart_modes_dates_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startMillis = dateFormat.parse(startStr)?.time ?: 0
            val endMillis = dateFormat.parse(endStr)?.time ?: 0

            if (startMillis >= endMillis) {
                Toast.makeText(this, getString(R.string.smart_modes_end_after_start), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentMode = getString(R.string.smart_modes_absent)
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
                        getString(R.string.smart_modes_absence_default_message, startStr, endStr)
                    }

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.smart_modes_absence_subject))
                        putExtra(Intent.EXTRA_TEXT, msg)
                    }

                    try {
                        startActivity(Intent.createChooser(intent, getString(R.string.smart_modes_email_chooser)))
                    } catch (e: Exception) {
                        Toast.makeText(this@SmartModesActivity, getString(R.string.smart_modes_no_email_app), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SmartModesActivity, getString(R.string.smart_modes_no_patient_email_or_appointments), Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun updateCurrentMode() {
        currentModeText.text = getString(R.string.smart_modes_current_mode_label, currentMode)

        // Reset all backgrounds to default
        homeVisitLayout.setBackgroundResource(R.drawable.rounded_border)
        doNotDisturbLayout.setBackgroundResource(R.drawable.rounded_border)
        absentLayout.setBackgroundResource(R.drawable.rounded_border)

        val homeVisit = getString(R.string.smart_modes_home_visit)
        val dnd = getString(R.string.smart_modes_do_not_disturb)
        val absent = getString(R.string.smart_modes_absent)

        when (currentMode) {
            homeVisit -> homeVisitLayout.setBackgroundResource(R.drawable.rounded_green_background)
            dnd -> doNotDisturbLayout.setBackgroundResource(R.drawable.rounded_green_background)
            absent -> absentLayout.setBackgroundResource(R.drawable.rounded_green_background)
        }

        doNotDisturbSwitch.isChecked = (currentMode == dnd)
    }
}
