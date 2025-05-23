package com.example.projetmediassist.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projetmediassist.models.Patient

@Dao
interface PatientDao {
    @Insert
    suspend fun insert(patient: Patient): Long

    @Query("SELECT * FROM patients WHERE doctorEmail = :email")
    suspend fun getPatientsForDoctor(email: String): List<Patient>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Int): Patient?

    @Query("SELECT * FROM patients WHERE fullName = :fullName LIMIT 1")
    suspend fun getPatientByFullName(fullName: String): Patient?

    @Query("SELECT * FROM patients WHERE fullName = :name LIMIT 1")
    suspend fun getPatientByName(name: String): Patient?


}
