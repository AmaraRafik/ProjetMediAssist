package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.utils.AuthUtils
import com.example.projetmediassist.utils.CalendarUtils
import com.example.projetmediassist.utils.SettingsUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import java.util.Locale
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater

class SettingsActivity : BaseActivity() {

    private val RC_SIGN_IN_FOR_CALENDAR = 9002
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var prefsName: String
    private lateinit var prefsEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        prefsName = prefs.getString("doctorName", "Médecin") ?: "Médecin"
        prefsEmail = prefs.getString("doctorEmail", "") ?: ""

        val doctorNameText = findViewById<TextView>(R.id.doctorNameText)
        doctorNameText.text = getString(R.string.settings_doctor_prefix, prefsName)

        // ======= Bloc LANGUE =======
        val languageLayout = findViewById<LinearLayout>(R.id.languageLayout)
        val langLabel = languageLayout.getChildAt(1) as? TextView

        // Affiche la langue actuelle (préférence sinon système)
        val langPref = prefs.getString("lang", null)
        val currentLang = langPref ?: Locale.getDefault().language
        langLabel?.text = if (currentLang == "fr") "Français >" else "English >"

        languageLayout.setOnClickListener {
            val cur = prefs.getString("lang", null) ?: Locale.getDefault().language
            val newLang = if (cur == "fr") "en" else "fr"

            // ----------- DIALOG PERSONNALISÉ -------------
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_restart_app, null)
            val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
            dialogMessage.text = getString(R.string.dialog_restart_message)

            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            dialogView.findViewById<Button>(R.id.yesButton).setOnClickListener {
                prefs.edit().putString("lang", newLang).apply()
                // Relance toute l'app proprement depuis SplashActivity
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                alertDialog.dismiss()
                // Pas besoin de killProcess/exitProcess
            }
            dialogView.findViewById<Button>(R.id.noButton).setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
            // ----------- FIN DIALOG -------------
        }
        // ==========================

        // Notifications
        val notificationSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.notificationSwitch)
        notificationSwitch.isChecked = SettingsUtils.isDoNotDisturbEnabled(this)

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        )
        val medicalBlue = ContextCompat.getColor(this, R.color.medical_blue)
        val gray = ContextCompat.getColor(this, R.color.gray)
        val lightMedicalBlue = ContextCompat.getColor(this, R.color.medical_blue)
        val lightGray = ContextCompat.getColor(this, R.color.gray)
        val thumbColors = intArrayOf(medicalBlue, gray)
        val thumbColorStateList = ColorStateList(states, thumbColors)
        notificationSwitch.thumbTintList = thumbColorStateList
        val trackColors = intArrayOf(lightMedicalBlue, lightGray)
        val trackColorStateList = ColorStateList(states, trackColors)
        notificationSwitch.trackTintList = trackColorStateList

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            SettingsUtils.setDoNotDisturb(this, isChecked)
            if (isChecked) {
                SettingsUtils.setCurrentMode(this, "Ne pas déranger")
                Toast.makeText(this, getString(R.string.mode_dnd_on), Toast.LENGTH_SHORT).show()
            } else {
                SettingsUtils.setCurrentMode(this, "En consultation")
                Toast.makeText(this, getString(R.string.mode_dnd_off), Toast.LENGTH_SHORT).show()
            }
        }

        // Google SignIn pour Agenda
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val logoutBtn = findViewById<TextView>(R.id.logoutButton)
        logoutBtn.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                prefs.edit().clear().apply()
                Toast.makeText(this, getString(R.string.settings_logout_toast), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
        }

        val syncButton = findViewById<Button>(R.id.syncButton)
        syncButton.setOnClickListener {
            val googleAccount = AuthUtils.getLastSignedInAccountWithCalendarScope(this)
            if (googleAccount != null && googleAccount.email != null) {
                launchCalendarSync(googleAccount.email!!)
            } else {
                Toast.makeText(this, "Autorisation Google Agenda requise.", Toast.LENGTH_SHORT).show()
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN_FOR_CALENDAR)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN_FOR_CALENDAR) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null && account.email != null &&
                    GoogleSignIn.hasPermissions(account, Scope(CalendarScopes.CALENDAR))) {
                    Toast.makeText(this, "Autorisation Google Agenda accordée.", Toast.LENGTH_SHORT).show()
                    launchCalendarSync(account.email!!)
                } else {
                    Toast.makeText(this, "Permission Google Agenda refusée ou compte invalide.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Log.w("SettingsActivity", "Google Sign-In failed for calendar sync", e)
                Toast.makeText(this, "Échec de la connexion Google: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun launchCalendarSync(googleAccountEmail: String) {
        if (prefsEmail.isEmpty()) {
            Toast.makeText(this, "Email du médecin non configuré pour la synchronisation locale.", Toast.LENGTH_LONG).show()
            return
        }
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = db.appointmentDao()
            val appointments = dao.getAppointmentsForDoctor(prefsEmail)
            var successCount = 0
            var failCount = 0
            if (appointments.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Aucun rendez-vous local à synchroniser.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            for (appointment in appointments) {
                if (appointment.googleEventId == null) {
                    val createdEventId = CalendarUtils.addEvent(
                        context = this@SettingsActivity,
                        accountName = googleAccountEmail,
                        title = "RDV MediAssist: ${appointment.patient}",
                        description = "Patient: ${appointment.patient}\nDescription: ${appointment.description ?: "N/A"}",
                        startMillis = appointment.timeInMillis
                    )
                    if (createdEventId != null) {
                        dao.updateGoogleEventId(appointment.id, createdEventId)
                        successCount++
                    } else {
                        failCount++
                    }
                }
            }
            withContext(Dispatchers.Main) {
                val message = when {
                    successCount > 0 && failCount == 0 -> "✅ $successCount RDV synchronisés avec Google Calendar."
                    successCount > 0 && failCount > 0 -> "⚠️ $successCount RDV synchronisés, $failCount échecs."
                    successCount == 0 && failCount > 0 -> "❌ Échec de synchronisation pour $failCount RDV."
                    else -> "Aucun nouveau RDV à synchroniser."
                }
                Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
