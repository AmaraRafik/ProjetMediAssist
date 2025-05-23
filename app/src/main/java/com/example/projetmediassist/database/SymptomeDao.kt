package com.example.projetmediassist.database

import androidx.room.*
import com.example.projetmediassist.models.Symptome

@Dao
interface SymptomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptome: Symptome): Long

    @Query("SELECT * FROM symptomes")
    suspend fun getAll(): List<Symptome>

    @Query("SELECT * FROM symptomes WHERE id = :id")
    suspend fun getById(id: Int): Symptome?

}
