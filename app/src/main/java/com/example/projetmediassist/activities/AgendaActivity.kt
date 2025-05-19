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
import com.example.projetmediassist.fragments.AddAppointmentFragment
import com.example.projetmediassist.fragments.OnAppointmentAddedListener
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
    private var weekOffset = 0


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

        // ⏪ Boutons de navigation semaine
        findViewById<Button>(R.id.prevWeekButton).setOnClickListener {
            weekOffset--
            renderWeek(weekOffset)
        }

        findViewById<Button>(R.id.nextWeekButton).setOnClickListener {
            weekOffset++
            renderWeek(weekOffset)
        }

        // 🔁 Affiche la semaine actuelle
        renderWeek(weekOffset)

        // ➕ Ajout de rendez-vous
        val addButton = findViewById<Button>(R.id.addAppointmentButton)
        addButton.setOnClickListener {
            val selected = selectedDate?.clone() as? Calendar ?: Calendar.getInstance()
            selected.set(Calendar.HOUR_OF_DAY, 0)
            selected.set(Calendar.MINUTE, 0)
            selected.set(Calendar.SECOND, 0)
            selected.set(Calendar.MILLISECOND, 0)

            val fragment = AddAppointmentFragment()
            fragment.selectedDate = selected.timeInMillis

            fragment.listener = object : OnAppointmentAddedListener {
                override fun onAppointmentAdded() {
                    loadAppointments(selected.timeInMillis)
                }
            }

            fragment.show(supportFragmentManager, "AddAppointmentFragment")
        }
    }

    private fun renderWeek(offset: Int) {
        val dayStrip = findViewById<LinearLayout>(R.id.dayStrip)
        val dateLabel = findViewById<TextView>(R.id.dateLabel)
        val weekText = findViewById<TextView>(R.id.weekRangeText)
        val monthText = findViewById<TextView>(R.id.monthText)

        dayStrip.removeAllViews()

        val dayFormat = SimpleDateFormat("EEE", Locale("fr"))
        val dateFormat = SimpleDateFormat("d", Locale("fr"))
        val labelFormat = SimpleDateFormat("EEEE d MMMM", Locale("fr"))
        val rangeFormat = SimpleDateFormat("dd MMM", Locale("fr"))
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("fr"))

        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.add(Calendar.WEEK_OF_YEAR, offset)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val startDate = calendar.clone() as Calendar

        val startText = rangeFormat.format(startDate.time)

        for (i in 0 until 7) {
            val clone = calendar.clone() as Calendar
            val dayName = dayFormat.format(clone.time).uppercase()
            val dayNumber = dateFormat.format(clone.time)

            val dayView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(6, 0, 6, 0)
                }

                setPadding(8, 16, 8, 16)
                gravity = Gravity.CENTER
                textSize = 14f
                text = "$dayName\n$dayNumber"
                typeface = Typeface.DEFAULT_BOLD
                tag = clone

                // style si sélectionné
                if (i == 0) {
                    selectedDayView = this
                    selectedDate = clone
                    setBackgroundResource(R.drawable.selected_day_background)
                    setTextColor(Color.WHITE)
                    dateLabel.text = labelFormat.format(clone.time).replaceFirstChar { it.uppercase() }
                    loadAppointments(clone.timeInMillis)
                }

                setOnClickListener {
                    selectedDayView?.setBackgroundResource(0)
                    selectedDayView?.setTextColor(Color.BLACK)

                    setBackgroundResource(R.drawable.selected_day_background)
                    setTextColor(Color.WHITE)
                    selectedDayView = this

                    val normalized = (clone.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    selectedDate = normalized

                    dateLabel.text = labelFormat.format(normalized.time).replaceFirstChar { it.uppercase() }
                    loadAppointments(normalized.timeInMillis)
                }

                if (i == 0) performClick()


            }

            dayStrip.addView(dayView)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val endText = rangeFormat.format(calendar.time)
        weekText.text = "$startText - $endText"
        monthText.text = monthFormat.format(calendar.time)
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

        val today = selectedDate?.clone() as? Calendar ?: Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        loadAppointments(today.timeInMillis)
    }
}
