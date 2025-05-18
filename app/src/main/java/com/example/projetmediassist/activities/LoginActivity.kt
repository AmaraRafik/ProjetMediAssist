package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val doctor = db.doctorDao().getDoctorByEmail(email)
                    withContext(Dispatchers.Main) {
                        when {
                            doctor == null -> {
                                Toast.makeText(this@LoginActivity, "Email non trouvé", Toast.LENGTH_SHORT).show()
                            }
                            doctor.password != password -> {
                                Toast.makeText(this@LoginActivity, "Mot de passe incorrect", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                // ---- MODIF : Sauvegarder la session ----
                                val sharedPref = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                                sharedPref.edit().putString("doctorEmail", doctor.email).apply()

                                val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                                intent.putExtra("doctorName", doctor.fullName)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        binding.createAccountText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
