package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptomes")
data class Symptome(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String,
    val synonymes: String // Ex : "fievre,temperature,temp"
)
