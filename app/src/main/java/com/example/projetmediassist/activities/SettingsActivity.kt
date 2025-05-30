package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
// Assurez-vous que EnterGmailDialogFragment n'est plus nécessaire si vous suivez cette logique
// import com.example.projetmediassist.fragments.EnterGmailDialogFragment
import com.example.projetmediassist.utils.AuthUtils
import com.example.projetmediassist.utils.CalendarUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes // Crucial import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private val RC_SIGN_IN_FOR_CALENDAR = 9002 // Code de requête pour Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var prefsName: String // Pour le nom du médecin dans les SharedPreferences
    private lateinit var prefsEmail: String // Pour l'email du médecin (local) dans les SharedPreferences

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
            .requestScopes(Scope(CalendarScopes.CALENDAR)) // Demande explicite du scope Agenda
            // Si vous utilisez un ID client web pour Firebase, vous pouvez l'ajouter ici aussi,
            // mais pour l'API Calendar client-side, requestScopes est le plus important.
            // .requestIdToken(getString(R.string.default_web_client_id))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val logoutBtn = findViewById<TextView>(R.id.logoutButton)
        logoutBtn.setOnClickListener {
            // Déconnexion Firebase (si géré par Firebase Auth)
            // FirebaseAuth.getInstance().signOut()

            // Déconnexion Google Sign-In
            googleSignInClient.signOut().addOnCompleteListener {
                // Effacer les SharedPreferences après la déconnexion complète
                prefs.edit().clear().apply()
                Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity() // Termine toutes les activités
            }
        }

        val syncButton = findViewById<Button>(R.id.syncButton)
        syncButton.setOnClickListener {
            // Vérifier si l'utilisateur est connecté et a accordé la permission Calendar
            val googleAccount = AuthUtils.getLastSignedInAccountWithCalendarScope(this)
            if (googleAccount != null && googleAccount.email != null) {
                // Si oui, lancer la synchronisation
                launchCalendarSync(googleAccount.email!!)
            } else {
                // Sinon, initier le processus de connexion/autorisation Google
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
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "Notifications activées" else "Notifications désactivées",
                Toast.LENGTH_SHORT
            ).show()
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
                    // L'utilisateur s'est connecté et a accordé la permission
                    Toast.makeText(this, "Autorisation Google Agenda accordée.", Toast.LENGTH_SHORT).show()
                    launchCalendarSync(account.email!!)
                } else {
                    // L'utilisateur s'est connecté mais n'a pas accordé la permission
                    Toast.makeText(this, "Permission Google Agenda refusée ou compte invalide.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                // Échec de la connexion Google
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

        lifecycleScope.launch(Dispatchers.IO) { // Utiliser Dispatchers.IO pour les opérations DB et réseau
            val dao = db.appointmentDao()
            // Utiliser prefsEmail (l'email du docteur stocké localement) pour récupérer les RDV
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
                if (appointment.googleEventId == null) { // Ne synchroniser que les RDV non encore liés
                    val createdEventId = CalendarUtils.addEvent(
                        context = this@SettingsActivity,
                        accountName = googleAccountEmail, // Email du compte Google pour l'API Calendar
                        title = "RDV MediAssist: ${appointment.patient}",
                        description = "Patient: ${appointment.patient}\nDescription: ${appointment.description ?: "N/A"}",
                        startMillis = appointment.timeInMillis
                        // endMillis peut être calculé si vous avez une durée standard de RDV
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