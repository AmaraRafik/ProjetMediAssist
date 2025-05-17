package com.example.projetmediassist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projetmediassist.models.Doctor

@Database(entities = [Doctor::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun doctorDao(): DoctorDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "mediassist.db"
                ).build().also { instance = it }
            }
    }
}
