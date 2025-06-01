package com.example.projetmediassist.activities

import android.app.AlertDialog
import android.content.Intent
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
import com.example.projetmediassist.utils.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AgendaActivity : BaseActivity() {

    private var selectedDayView: TextView? = null
    private var selectedDate: Calendar? = null
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentAdapter
    private lateinit var doctorEmail: String
    private var weekOffset = 0

    private var prefillPatientName: String? = null
    private var prefillPatientEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        prefillPatientName = intent.getStringExtra("prefill_patient_name")
        prefillPatientEmail = intent.getStringExtra("prefill_patient_email")

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        doctorEmail = prefs.getString("doctorEmail", null) ?: run {
            Toast.makeText(this, getString(R.string.agenda_error_doctor_not_connected), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db = AppDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.appointmentsRecycler)

        findViewById<Button>(R.id.prevWeekButton).setOnClickListener {
            weekOffset--
            renderWeek(weekOffset)
            selectedDayView?.performClick()
        }

        findViewById<Button>(R.id.nextWeekButton).setOnClickListener {
            weekOffset++
            renderWeek(weekOffset)
            selectedDayView?.performClick()
        }

        renderWeek(weekOffset)

        selectedDayView?.performClick()

        findViewById<Button>(R.id.addAppointmentButton).setOnClickListener {
            val selected = selectedDate?.clone() as? Calendar ?: Calendar.getInstance()
            selected.set(Calendar.HOUR_OF_DAY, 0)
            selected.set(Calendar.MINUTE, 0)
            selected.set(Calendar.SECOND, 0)
            selected.set(Calendar.MILLISECOND, 0)

            val fragment = AddAppointmentFragment()
            fragment.selectedDate = selected.timeInMillis

            val args = Bundle().apply {
                prefillPatientName?.let { putString("prefill_patient_name", it) }
                prefillPatientEmail?.let { putString("prefill_patient_email", it) }
            }
            fragment.arguments = args

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

        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            add(Calendar.WEEK_OF_YEAR, offset)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var todayWasSelected = false

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

                val isToday = clone.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        clone.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

                if (isToday && !todayWasSelected) {
                    selectedDayView = this
                    selectedDate = clone
                    setBackgroundResource(R.drawable.selected_day_background)
                    setTextColor(Color.WHITE)
                    dateLabel.text = labelFormat.format(clone.time).replaceFirstChar { it.uppercase() }
                    todayWasSelected = true
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
            }

            dayStrip.addView(dayView)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        if (!todayWasSelected && dayStrip.childCount > 0) {
            val firstDay = dayStrip.getChildAt(0) as TextView
            firstDay.performClick()
        }

        val endText = rangeFormat.format(calendar.time)
        weekText.text = "${
            rangeFormat.format((calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -7) }.time)
        } - $endText"
        monthText.text = monthFormat.format(calendar.time)
    }

    private fun loadAppointments(date: Long) {
        lifecycleScope.launch {
            val list = db.appointmentDao().getAppointmentsForDoctorOnDate(doctorEmail, date)

            withContext(Dispatchers.Main) {
                adapter = AppointmentAdapter(
                    list,
                    { appointmentToDelete ->
                        AlertDialog.Builder(this@AgendaActivity)
                            .setTitle(getString(R.string.agenda_delete_dialog_title))
                            .setMessage(getString(R.string.agenda_delete_dialog_message))
                            .setPositiveButton(getString(R.string.common_yes)) { _, _ ->
                                lifecycleScope.launch {
                                    NotificationUtils.cancelAppointmentNotifications(
                                        context = this@AgendaActivity,
                                        appointmentTime = appointmentToDelete.timeInMillis
                                    )
                                    db.appointmentDao().delete(appointmentToDelete)
                                    withContext(Dispatchers.Main) {
                                        loadAppointments(date)
                                        Toast.makeText(
                                            this@AgendaActivity,
                                            getString(R.string.agenda_deleted_appointment_toast),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            .setNegativeButton(getString(R.string.common_no), null)
                            .show()
                    },
                    { clickedAppointment ->
                        val intent = Intent(this@AgendaActivity, DetailAppointmentActivity::class.java)
                        intent.putExtra("APPOINTMENT_ID", clickedAppointment.id)
                        startActivity(intent)
                    }
                )
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@AgendaActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        selectedDate?.timeInMillis?.let { loadAppointments(it) }
    }
}
