package com.example.projetmediassist.database

import androidx.room.*
import com.example.projetmediassist.models.Appointment

@Dao
interface AppointmentDao {

    @Insert
    suspend fun insert(appointment: Appointment)

    @Query("SELECT * FROM appointments WHERE doctorEmail = :doctorEmail AND date = :date")
    suspend fun getAppointmentsForDoctorOnDate(doctorEmail: String, date: Long): List<Appointment>

    @Delete
    suspend fun delete(appointment: Appointment)
}
