package com.example.projetmediassist.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projetmediassist.models.AssociationMaladieMedicament

@Dao
interface AssociationMaladieMedicamentDao {
    @Insert
    suspend fun insert(assoc: AssociationMaladieMedicament)

    @Query("SELECT medicamentNom FROM AssociationMaladieMedicament WHERE maladieNom = :maladieNom")
    suspend fun getMedicamentsForMaladie(maladieNom: String): List<String>
}
