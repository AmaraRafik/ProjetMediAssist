package com.example.projetmediassist.activities

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmediassist.R
import com.example.projetmediassist.adapters.AppointmentAdapter
import com.example.projetmediassist.models.Appointment
import java.text.SimpleDateFormat
import java.util.*

class AgendaActivity : AppCompatActivity() {

    private var selectedDayView: TextView? = null
    private var selectedDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

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
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(2, 0, 2, 0)
                }

                setPadding(4, 8, 4, 8)
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                textSize = 14f
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                maxLines = 2
                text = "$dayName\n$dayNumber"

                tag = currentDay.clone() // stocker la date

                if (i == 0) {
                    setBackgroundResource(R.drawable.selected_day_background)
                    setTextColor(Color.WHITE)
                    selectedDayView = this
                    selectedDate = currentDay.clone() as Calendar
                    dateLabel.text = labelFormat.format(currentDay.time).replaceFirstChar { it.uppercase() }
                } else {
                    setTextColor(Color.BLACK)
                }

                setOnClickListener {
                    // Réinitialiser la sélection précédente
                    selectedDayView?.setBackgroundResource(0)
                    selectedDayView?.setTextColor(Color.BLACK)

                    // Appliquer le nouveau style
                    setBackgroundResource(R.drawable.selected_day_background)
                    setTextColor(Color.WHITE)

                    // Mettre à jour l'état
                    selectedDayView = this
                    selectedDate = this.tag as Calendar

                    val formatted = labelFormat.format(selectedDate!!.time)
                    dateLabel.text = formatted.replaceFirstChar { it.uppercase() }


                }
            }

            dayStrip.addView(dayView)
            currentDay.add(Calendar.DAY_OF_MONTH, 1)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.appointmentsRecycler)

        val appointmentList = listOf(
            Appointment(
                doctorEmail = "dr.dupont@gmail.com", // valeur fictive
                hour = "09:00",
                patient = "Yassir Chbouk",
                description = "Consultation générale",
                date = System.currentTimeMillis()
            )
        )


        recyclerView.adapter = AppointmentAdapter(appointmentList)
        recyclerView.layoutManager = LinearLayoutManager(this)

    }
}
