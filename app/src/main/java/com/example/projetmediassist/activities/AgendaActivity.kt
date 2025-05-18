package com.example.projetmediassist.activities

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.adapters.AppointmentAdapter
import com.example.projetmediassist.database.AppDatabase
import com.example.projetmediassist.models.Appointment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AgendaActivity : AppCompatActivity() {

    private var selectedDayView: TextView? = null
    private var selectedDate: Calendar? = null
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentAdapter
    private lateinit var doctorEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        doctorEmail = prefs.getString("doctorEmail", null) ?: run {
            Toast.makeText(this, "Médecin non connecté", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db = AppDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.appointmentsRecycler)

        val dayStrip = findViewById<LinearLayout>(R.id.dayStrip)
        val monthText = findViewById<TextView>(R.id.monthText)
        val dateLabel = findViewById<TextView>(R.id.dateLabel)

        val today = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("fr"))
        monthText.text = monthFormat.format(today.time)

        val dayFormat = SimpleDateFormat("EEE", Locale("fr"))
        val dateFormat = SimpleDateFormat("d", Locale("fr"))
        val labelFormat = SimpleDateFormat("EEEE d MMMM", Locale("fr"))

        val currentDay = Calendar.getInstance()

        for (i in 0 until 7) {
            val dayName = dayFormat.format(currentDay.time).uppercase()
            val dayNumber = dateFormat.format(currentDay.time)

            val dayView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(2, 0, 2, 0)
                }

                setPadding(4, 8, 4, 8)
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                textSize = 14f
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                maxLines = 2
                text = "$dayName\n$dayNumber"
                tag = currentDay.clone()

                if (i == 0) {
                    setBackgroundResource(R.drawable.selected_day_background)
                    setTextColor(Color.WHITE)
                    selectedDayView = this
                    selectedDate = currentDay.clone() as Calendar
                    dateLabel.text = labelFormat.format(currentDay.time).replaceFirstChar { it.uppercase() }

                    loadAppointments(currentDay.timeInMillis)
                } else {
                    setTextColor(Color.BLACK)
                }

                setOnClickListener {
                    selectedDayView?.setBackgroundResource(0)
                    selectedDayView?.setTextColor(Color.BLACK)

                    setBackgroundResource(R.drawable.selected_day_background)
                    setTextColor(Color.WHITE)

                    selectedDayView = this
                    selectedDate = this.tag as Calendar

                    val formatted = labelFormat.format(selectedDate!!.time)
                    dateLabel.text = formatted.replaceFirstChar { it.uppercase() }

                    val normalized = (selectedDate as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    loadAppointments(normalized.timeInMillis)
                }
            }

            dayStrip.addView(dayView)
            currentDay.add(Calendar.DAY_OF_MONTH, 1)
        }

        val addButton = findViewById<Button>(R.id.addAppointmentButton)
        addButton.setOnClickListener {
            insertAppointmentForToday()
        }
    }

    private fun insertAppointmentForToday() {
        val today = selectedDate?.clone() as? Calendar ?: Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val newAppointment = Appointment(
            doctorEmail = doctorEmail,
            hour = "14:00",
            patient = "Nouveau Patient",
            description = "Consultation ajoutée manuellement",
            date = today.timeInMillis
        )

        lifecycleScope.launch {
            db.appointmentDao().insert(newAppointment)
            withContext(Dispatchers.Main) {
                loadAppointments(today.timeInMillis)
            }
        }
    }

    private fun loadAppointments(date: Long) {
        lifecycleScope.launch {
            val list = db.appointmentDao().getAppointmentsForDoctorOnDate(doctorEmail, date)

            withContext(Dispatchers.Main) {
                adapter = AppointmentAdapter(list) { appointmentToDelete ->
                    AlertDialog.Builder(this@AgendaActivity)
                        .setTitle("Supprimer le rendez-vous")
                        .setMessage("Voulez-vous vraiment supprimer ce rendez-vous ?")
                        .setPositiveButton("Oui") { _, _ ->
                            lifecycleScope.launch {
                                db.appointmentDao().delete(appointmentToDelete)
                                withContext(Dispatchers.Main) {
                                    loadAppointments(date)
                                }
                            }
                        }
                        .setNegativeButton("Non", null)
                        .show()
                }
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@AgendaActivity)
            }
        }
    }
    override fun onResume() {
        super.onResume()

        // Recharge les RDV du jour sélectionné si possible
        val today = selectedDate?.clone() as? Calendar ?: Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        loadAppointments(today.timeInMillis)
    }
}
