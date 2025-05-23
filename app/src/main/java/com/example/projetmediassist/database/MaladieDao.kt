package com.example.projetmediassist.database

import androidx.room.*
import com.example.projetmediassist.models.Maladie

@Dao
interface MaladieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maladie: Maladie): Long

    @Query("SELECT COUNT(*) FROM maladies")
    suspend fun countMaladies(): Int


    @Query("SELECT * FROM maladies")
    suspend fun getAll(): List<Maladie>

    @Query("SELECT * FROM maladies WHERE id = :id")
    suspend fun getMaladieById(id: Int): Maladie?
}
