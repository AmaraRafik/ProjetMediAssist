package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Medicament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String,
    val description: String?,
    val contreIndications: String?, // ex: "allergie_penicilline,asthme"
    val interactions: String?, // ex: "paracetamol,ibuprofene"
    var posologie: String?
)
