package com.example.projetmediassist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projetmediassist.models.Appointment
import com.example.projetmediassist.models.Doctor
import com.example.projetmediassist.models.Patient

@Database(entities = [Doctor::class, Patient::class, Appointment::class], version = 5)
abstract class AppDatabase : RoomDatabase() {

    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "mediassist.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
