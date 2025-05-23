package com.example.projetmediassist.database

import androidx.room.*
import com.example.projetmediassist.models.AssociationSymptomeMaladie

@Dao
interface AssociationSymptomeMaladieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(association: AssociationSymptomeMaladie): Long

    @Query("SELECT * FROM association_symptome_maladie")
    suspend fun getAll(): List<AssociationSymptomeMaladie>

    @Query("SELECT * FROM association_symptome_maladie WHERE maladieId = :maladieId")
    suspend fun getForMaladie(maladieId: Int): List<AssociationSymptomeMaladie>

    @Query("SELECT * FROM association_symptome_maladie WHERE symptomeId = :symptomeId")
    suspend fun getForSymptome(symptomeId: Int): List<AssociationSymptomeMaladie>
}
