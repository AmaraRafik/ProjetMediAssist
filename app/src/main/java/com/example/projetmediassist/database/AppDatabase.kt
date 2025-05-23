package com.example.projetmediassist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projetmediassist.models.*

// AJOUTE ICI LES NOUVELLES ENTITÉS
import com.example.projetmediassist.models.Ordonnance
import com.example.projetmediassist.models.OrdonnanceMedicament

@Database(
    entities = [
        Doctor::class,
        Patient::class,
        Appointment::class,
        Symptome::class,
        Maladie::class,
        AssociationSymptomeMaladie::class,
        Medicament::class,
        AssociationMaladieMedicament::class,

        Ordonnance::class,
        OrdonnanceMedicament::class
    ],
    version = 11
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): AppointmentDao

    abstract fun symptomeDao(): SymptomeDao
    abstract fun maladieDao(): MaladieDao
    abstract fun associationSymptomeMaladieDao(): AssociationSymptomeMaladieDao

    abstract fun medicamentDao(): MedicamentDao
    abstract fun associationMaladieMedicamentDao(): AssociationMaladieMedicamentDao


    abstract fun ordonnanceDao(): OrdonnanceDao
    abstract fun ordonnanceMedicamentDao(): OrdonnanceMedicamentDao

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
