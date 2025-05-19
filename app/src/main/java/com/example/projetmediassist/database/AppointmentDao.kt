package com.example.projetmediassist.database

import androidx.room.*
import com.example.projetmediassist.models.Appointment

@Dao
interface AppointmentDao {

    @Insert
    suspend fun insert(appointment: Appointment)

    @Query("SELECT * FROM appointments WHERE doctorEmail = :email AND date = :day ORDER BY timeInMillis ASC")
    suspend fun getAppointmentsForDoctorOnDate(email: String, day: Long): List<Appointment>

    @Query("""
        SELECT * FROM appointments 
        WHERE doctorEmail = :email 
        AND timeInMillis > :now 
        ORDER BY timeInMillis ASC 
        LIMIT 1
    """)
    suspend fun getNextAppointment(email: String, now: Long): Appointment?

    @Delete
    suspend fun delete(appointment: Appointment)
}