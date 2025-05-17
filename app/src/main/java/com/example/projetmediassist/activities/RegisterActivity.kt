package com.example.projetmediassist.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            // TODO: Ajouter la logique d'inscription
        }

        binding.alreadyHaveAccountText.setOnClickListener {
            finish() // Retour à LoginActivity
        }
    }
}
