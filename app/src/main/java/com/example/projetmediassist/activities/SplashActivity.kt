package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.R
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            checkSessionAndStart()
        }, 2000) // 2 secondes
    }

    private fun checkSessionAndStart() {
        val sharedPref = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
        val savedEmail = sharedPref.getString("doctorEmail", null)

        if (savedEmail != null) {
            // Vérifie en base que le médecin existe toujours
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@SplashActivity)
                val doctor = db.doctorDao().getDoctorByEmail(savedEmail)
                if (doctor != null) {
                    val intent = Intent(this@SplashActivity, DashboardActivity::class.java)
                    intent.putExtra("doctorName", doctor.fullName)
                    startActivity(intent)
                    finish()
                } else {
                    // Session invalide, retour au login
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            }
        } else {
            // Pas de session, direction Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
