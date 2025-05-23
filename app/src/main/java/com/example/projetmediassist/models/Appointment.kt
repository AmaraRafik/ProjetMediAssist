package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorEmail: String,
    val patient: String,
    val patientEmail: String?, // ← nouveau champ
    val hour: String,
    val description: String,
    val date: Long,
    val timeInMillis: Long
)
