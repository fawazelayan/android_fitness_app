package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {
    @Query("SELECT * FROM daily_intake ORDER BY date DESC")
    fun getAllIntakes(): Flow<List<DailyIntake>>

    @Query("SELECT * FROM daily_intake WHERE date LIKE :profileId || '_%' ORDER BY date DESC")
    fun getIntakesForProfile(profileId: String): Flow<List<DailyIntake>>

    @Query("SELECT * FROM daily_intake WHERE date = :date LIMIT 1")
    suspend fun getIntakeForDate(date: String): DailyIntake?

    @Query("SELECT * FROM daily_intake WHERE date = :date LIMIT 1")
    fun getIntakeFlowForDate(date: String): Flow<DailyIntake?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: DailyIntake)

    @Query("DELETE FROM daily_intake")
    suspend fun deleteAllIntakes()

    @Query("SELECT * FROM user_settings")
    fun getAllSettings(): Flow<List<UserSetting>>

    @Query("SELECT * FROM user_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): UserSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: UserSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<UserSetting>)

    // Water Log queries
    @Query("SELECT * FROM water_log WHERE profileId = :profileId AND date = :date ORDER BY timestamp DESC")
    fun getWaterLogs(profileId: String, date: String): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLog)

    @Query("DELETE FROM water_log WHERE id = :id")
    suspend fun deleteWaterLog(id: Int)

    @Query("DELETE FROM water_log")
    suspend fun deleteAllWaterLogs()

    // Food Log queries
    @Query("SELECT * FROM food_log WHERE profileId = :profileId AND date = :date ORDER BY timestamp DESC")
    fun getFoodLogs(profileId: String, date: String): Flow<List<FoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLog)

    @Query("DELETE FROM food_log WHERE id = :id")
    suspend fun deleteFoodLog(id: Int)

    @Query("DELETE FROM food_log")
    suspend fun deleteAllFoodLogs()
}
