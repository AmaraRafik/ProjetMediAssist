package com.example.projetmediassist.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projetmediassist.models.Medicament

@Dao
interface MedicamentDao {

    @Query("SELECT * FROM Medicament")
    suspend fun getAll(): List<Medicament>

    @Query("SELECT * FROM Medicament WHERE id = :id")
    suspend fun getById(id: Int): Medicament?

    @Query("SELECT * FROM Medicament WHERE nom IN (:noms)")
    suspend fun getByNoms(noms: List<String>): List<Medicament>

    @Insert
    suspend fun insertAll(vararg medicaments: Medicament)
}