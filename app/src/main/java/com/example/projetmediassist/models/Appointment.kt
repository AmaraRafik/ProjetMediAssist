package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorEmail: String, // ← DOIT exister exactement sous ce nom
    val hour: String,
    val patient: String,
    val description: String,
    val date: Long // ← DOIT exister exactement sous ce nom
)
