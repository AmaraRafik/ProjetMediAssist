package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorEmail: String,
    val patient: String,
    val patientEmail: String?,
    val patientId: Int = -1,
    val hour: String,
    val description: String?, // → rendu nullable
    val date: Long,
    val timeInMillis: Long,
    val googleEventId: String? = null // → nouveau champ pour lier un événement Google
)
