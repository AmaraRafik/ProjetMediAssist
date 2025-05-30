package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.utils.AuthUtils
import com.example.projetmediassist.utils.CalendarUtils
import com.example.projetmediassist.utils.SettingsUtils // Importez SettingsUtils ici
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Import nécessaire pour ColorStateList si vous utilisez la modification en Kotlin (sinon, pas nécessaire)
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

class SettingsActivity : AppCompatActivity() {

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
        doctorNameText.text = "Dr. $prefsName"

        // Configuration de GoogleSignInClient pour demander le scope Calendar
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val logoutBtn = findViewById<TextView>(R.id.logoutButton)
        logoutBtn.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                prefs.edit().clear().apply()
                Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show()
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

        val languageLayout = findViewById<LinearLayout>(R.id.languageLayout)
        languageLayout.setOnClickListener {
            Toast.makeText(this, "Changement de langue à venir", Toast.LENGTH_SHORT).show()
        }

        val notificationSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.notificationSwitch)

        // Initialiser l'état du switch en fonction du mode "Ne pas déranger" actuel
        notificationSwitch.isChecked = SettingsUtils.isDoNotDisturbEnabled(this)

        // (Optionnel) Code pour la couleur du switch en Kotlin - si vous le gardez ici
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
        // (Fin de la section optionnelle pour la couleur en Kotlin)


        // Modifier le comportement du switch de notification pour activer/désactiver le mode "Ne pas déranger"
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Utilisez SettingsUtils pour définir l'état du mode "Ne pas déranger"
            SettingsUtils.setDoNotDisturb(this, isChecked)

            // Mettez également à jour le mode courant général dans SettingsUtils
            // Cela assure que SmartModesActivity et DashboardActivity (si elles lisent le mode courant)
            // sont au courant de ce changement.
            if (isChecked) {
                SettingsUtils.setCurrentMode(this, "Ne pas déranger")
                Toast.makeText(this, getString(R.string.mode_dnd_on), Toast.LENGTH_SHORT).show()
            } else {
                // Quand DND est désactivé, vous pourriez vouloir revenir à "En consultation"
                // ou au mode précédent si vous gérez un historique des modes.
                // Pour simplifier, on revient à "En consultation".
                SettingsUtils.setCurrentMode(this, "En consultation")
                Toast.makeText(this, getString(R.string.mode_dnd_off), Toast.LENGTH_SHORT).show()
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