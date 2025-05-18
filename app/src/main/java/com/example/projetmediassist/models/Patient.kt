package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val age: Int,
    val lastAppointment: String, // Format simple
    val doctorEmail: String // <-- clé étrangère logique, référence à Doctor
)
