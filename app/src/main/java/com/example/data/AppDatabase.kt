package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyIntake::class, UserSetting::class, WaterLog::class, FoodLog::class, Supplement::class, SupplementLog::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val intakeDao: IntakeDao
    abstract val supplementDao: SupplementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scoop_tracker_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
