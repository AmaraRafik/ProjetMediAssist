// models/AssociationSymptomeMaladie.kt
package com.example.projetmediassist.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "association_symptome_maladie")
data class AssociationSymptomeMaladie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symptomeId: Int,
    val maladieId: Int
)
