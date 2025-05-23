package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AssociationMaladieMedicament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val maladieNom: String,
    val medicamentNom: String
)
