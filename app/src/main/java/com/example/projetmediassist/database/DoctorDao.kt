package com.example.projetmediassist.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projetmediassist.models.Doctor

@Dao
interface DoctorDao {
    @Insert
    suspend fun insert(doctor: Doctor): Long

    @Query("SELECT * FROM doctors WHERE email = :email LIMIT 1")
    suspend fun getDoctorByEmail(email: String): Doctor?
}
