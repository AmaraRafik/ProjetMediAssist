package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.R
import com.example.projetmediassist.utils.MedicalDatabaseInitializer
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            checkSessionAndStart()
        }, 2000) // 2 secondes
    }

    private fun checkSessionAndStart() {
        val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
        val savedEmail = sharedPref.getString("doctorEmail", null)
        val firstLaunch = sharedPref.getBoolean("first_medical_data", true)
        val db = AppDatabase.getDatabase(this)

        if (firstLaunch) {
            // Insère la base médicale
            MedicalDatabaseInitializer.peuplerBaseMedicaleSiVide(this) {
                // Update flag pour ne pas réinsérer à chaque lancement
                sharedPref.edit().putBoolean("first_medical_data", false).apply()
                goToNextScreen(savedEmail, db)
            }
        } else {
            goToNextScreen(savedEmail, db)
        }
    }

    private fun goToNextScreen(savedEmail: String?, db: AppDatabase) {
        if (savedEmail != null) {
            lifecycleScope.launch {
                val doctor = db.doctorDao().getDoctorByEmail(savedEmail)
                if (doctor != null) {
                    val intent = Intent(this@SplashActivity, DashboardActivity::class.java)
                    intent.putExtra("doctorName", doctor.fullName)
                    startActivity(intent)
                    finish()
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
