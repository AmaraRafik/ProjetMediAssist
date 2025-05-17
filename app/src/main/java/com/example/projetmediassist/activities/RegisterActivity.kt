package com.example.projetmediassist.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.ActivityRegisterBinding
import com.example.projetmediassist.models.Doctor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)

        binding.registerButton.setOnClickListener {
            val name = binding.fullNameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val rpps = binding.rppsEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Vérification email
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
            if (!email.matches(emailPattern)) {
                Toast.makeText(this, "Veuillez saisir un email valide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!rpps.matches("\\d{11}".toRegex())) {
                Toast.makeText(this, "Numéro RPPS invalide (11 chiffres attendus)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Vérification mot de passe (8 caractères, minuscule, majuscule, chiffre, caractère spécial)
            val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}\$".toRegex()
            if (!password.matches(passwordPattern)) {
                Toast.makeText(
                    this,
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (name.isNotEmpty() && email.isNotEmpty() && rpps.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val existingDoctor = db.doctorDao().getDoctorByEmail(email)
                    if (existingDoctor != null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterActivity, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        db.doctorDao().insert(Doctor(fullName = name, email = email, rpps = rpps, password = password))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterActivity, "Compte créé !", Toast.LENGTH_SHORT).show()
                            finish() // Retour à la connexion
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        binding.alreadyHaveAccountText.setOnClickListener {
            finish()
        }
    }
}
