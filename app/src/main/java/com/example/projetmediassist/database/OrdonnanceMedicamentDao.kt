package com.example.projetmediassist.database

import androidx.room.*
import com.example.projetmediassist.models.OrdonnanceMedicament

@Dao
interface OrdonnanceMedicamentDao {
    @Insert
    suspend fun insertAll(medicaments: List<OrdonnanceMedicament>)

    @Query("SELECT * FROM OrdonnanceMedicament WHERE ordonnanceId = :ordonnanceId")
    suspend fun getByOrdonnance(ordonnanceId: Int): List<OrdonnanceMedicament>
}
