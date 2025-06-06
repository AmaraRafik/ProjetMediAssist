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

    @Query("SELECT * FROM appointments WHERE doctorEmail = :email ORDER BY timeInMillis ASC")
    suspend fun getAppointmentsForDoctor(email: String): List<Appointment>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: Int): Appointment?

    @Query("SELECT * FROM appointments WHERE doctorEmail = :email AND timeInMillis > :fromTime ORDER BY timeInMillis ASC")
    suspend fun getNextAppointmentsFromNow(email: String, fromTime: Long): List<Appointment>

    @Query("SELECT * FROM appointments WHERE doctorEmail = :email AND timeInMillis >= :now ORDER BY timeInMillis ASC")
    suspend fun getUpcomingAppointments(email: String, now: Long): List<Appointment>

    @Query("UPDATE appointments SET googleEventId = :eventId WHERE id = :appointmentId")
    suspend fun updateGoogleEventId(appointmentId: Int, eventId: String)

    @Query("DELETE FROM appointments WHERE doctorEmail = :email AND timeInMillis BETWEEN :start AND :end")
    suspend fun deleteAppointmentsInRange(email: String, start: Long, end: Long)

}