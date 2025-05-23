package com.example.projetmediassist.database

import androidx.room.*
import com.example.projetmediassist.models.Ordonnance

@Dao
interface OrdonnanceDao {
    @Insert
    suspend fun insert(ordonnance: Ordonnance): Long

    @Query("SELECT * FROM Ordonnance WHERE patientName = :patientName")
    suspend fun getOrdonnancesByPatient(patientName: String): List<Ordonnance>
}
