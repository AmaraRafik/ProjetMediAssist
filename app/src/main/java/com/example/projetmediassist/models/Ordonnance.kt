package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Ordonnance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val doctorName: String,
    val diagnostic: String,
    val date: Long = System.currentTimeMillis()
)
