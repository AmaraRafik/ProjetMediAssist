package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OrdonnanceMedicament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ordonnanceId: Int,      // Foreign key
    val nom: String,
    val posologie: String
)
