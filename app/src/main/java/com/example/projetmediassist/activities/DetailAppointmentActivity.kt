package com.example.projetmediassist.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projetmediassist.R
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.fragments.AddPatientFragment
import kotlinx.coroutines.*
import com.google.android.material.button.MaterialButton

class DetailAppointmentActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var appointmentId: Int = -1

    private lateinit var patientNameText: TextView
    private lateinit var dateHourText: TextView
    private lateinit var visitTypeText: TextView
    private lateinit var addressText: TextView
    private lateinit var viewPatientBtn: MaterialButton
    private lateinit var startConsultationBtn: MaterialButton

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_appointment)

        // UI elements
        patientNameText = findViewById(R.id.patientNameText)
        dateHourText = findViewById(R.id.dateHourText)
        visitTypeText = findViewById(R.id.visitTypeText)
        addressText = findViewById(R.id.addressText)
        viewPatientBtn = findViewById(R.id.viewPatientBtn)
        startConsultationBtn = findViewById(R.id.startConsultationBtn)

        // DB
        db = AppDatabase.getDatabase(this)

        // Get appointment ID
        appointmentId = intent.getIntExtra("APPOINTMENT_ID", -1)
        if (appointmentId == -1) {
            Toast.makeText(this, "Erreur : RDV introuvable", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Load data
        scope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        withContext(Dispatchers.IO) {
            // Récupère le RDV directement par ID
            val appointment = db.appointmentDao().getAppointmentById(appointmentId)
                ?: return@withContext runOnUiThread {
                    Toast.makeText(this@DetailAppointmentActivity, "Rendez-vous non trouvé", Toast.LENGTH_LONG).show()
                    finish()
                }

            val patient = db.patientDao().getPatientByFullName(appointment.patient)

            runOnUiThread {
                // Infos patient
                patientNameText.text = appointment.patient
                dateHourText.text = "${formatDate(appointment.date)} à ${appointment.hour}"

                // Visite à domicile/cabinet
                if (appointment.description?.contains("domicile", ignoreCase = true) == true) {
                    visitTypeText.text = "Visite à domicile"
                    addressText.text = patient?.address ?: "(Adresse inconnue)"
                    addressText.visibility = View.VISIBLE
                }else {
                    visitTypeText.text = "Visite au cabinet"
                    addressText.text = "Cabinet"
                    addressText.visibility = View.VISIBLE
                }

                viewPatientBtn.setOnClickListener {
                    scope.launch {
                        val patient = withContext(Dispatchers.IO) {
                            db.patientDao().getPatientByFullName(patientNameText.text.toString())
                        }
                        if (patient != null) {
                            val intent = Intent(this@DetailAppointmentActivity, PatientDetailActivity::class.java)
                            intent.putExtra("patient_id", patient.id)
                            startActivity(intent)
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@DetailAppointmentActivity,
                                    "Patient non trouvé, veuillez l'ajouter.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val fragment = AddPatientFragment.newInstance(
                                    fullName = appointment.patient,
                                    email = appointment.patientEmail
                                )
                                fragment.show(supportFragmentManager, "AddPatientFragment")
                            }
                        }
                    }
                }


                startConsultationBtn.setOnClickListener {
                    val intent = Intent(this@DetailAppointmentActivity, ConsultationActivity::class.java)
                    intent.putExtra("patient_name", patientNameText.text.toString())
                    startActivity(intent)
                }

            }
        }
    }


    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("EEEE d MMMM yyyy", java.util.Locale("fr"))
        return sdf.format(java.util.Date(timestamp)).replaceFirstChar { it.uppercase() }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
