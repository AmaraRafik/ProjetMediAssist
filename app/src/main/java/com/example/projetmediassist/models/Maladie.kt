// models/Maladie.kt
package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maladies")
data class Maladie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String,
    val description: String? = null
)
