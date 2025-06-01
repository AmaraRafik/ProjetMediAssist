package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.databinding.ActivityLoginBinding
import com.example.projetmediassist.fragments.EnterRppsDialogFragment
import com.example.projetmediassist.models.Doctor
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)
        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar"))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInButton.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val doctor = db.doctorDao().getDoctorByEmail(email)
                    withContext(Dispatchers.Main) {
                        when {
                            doctor == null -> showToast(getString(R.string.login_error_email_not_found))
                            doctor.password != password -> showToast(getString(R.string.login_error_wrong_password))
                            else -> saveSessionAndGoToDashboard(doctor.fullName, doctor.email)
                        }
                    }
                }
            } else {
                showToast(getString(R.string.login_error_empty_fields))
            }
        }

        binding.createAccountText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                showToast(getString(R.string.login_error_google, e.message ?: ""))
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val email = user?.email ?: return@addOnCompleteListener
                    val fullName = user.displayName ?: "Docteur"

                    val db = AppDatabase.getDatabase(this)

                    lifecycleScope.launch {
                        val doctor = db.doctorDao().getDoctorByEmail(email)
                        if (doctor != null) {
                            saveSessionAndGoToDashboard(doctor.fullName, doctor.email)
                        } else {
                            withContext(Dispatchers.Main) {
                                EnterRppsDialogFragment { rpps ->
                                    lifecycleScope.launch {
                                        val newDoctor = Doctor(
                                            fullName = fullName,
                                            email = email,
                                            rpps = rpps,
                                            password = ""
                                        )
                                        db.doctorDao().insert(newDoctor)
                                        saveSessionAndGoToDashboard(fullName, email)
                                    }
                                }.show(supportFragmentManager, "rppsDialog")
                            }
                        }
                    }
                } else {
                    showToast(getString(R.string.login_error_firebase))
                }
            }
    }

    private fun saveSessionAndGoToDashboard(name: String, email: String) {
        getSharedPreferences("session", MODE_PRIVATE).edit()
            .putString("doctorEmail", email)
            .putString("doctorName", name)
            .apply()

        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
