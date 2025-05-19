package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorEmail: String,
    val patient: String,
    val hour: String,
    val description: String,
    val date: Long, // jour sélectionné
    val timeInMillis: Long // ← ce champ va permettre le tri réel
)
