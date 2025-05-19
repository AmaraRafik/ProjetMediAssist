package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val age: Int,
    val lastAppointment: String,
    val doctorEmail: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val medicalHistory: String?,
    val allergies: String?
)
