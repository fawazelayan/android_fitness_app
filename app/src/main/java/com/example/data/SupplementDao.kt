package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementDao {
    @Query("SELECT * FROM supplement WHERE profileId = :profileId ORDER BY id ASC")
    fun getAllSupplements(profileId: String): Flow<List<Supplement>>

    @Query("SELECT * FROM supplement WHERE id = :id LIMIT 1")
    suspend fun getSupplementById(id: Int): Supplement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: Supplement)

    @Update
    suspend fun updateSupplement(supplement: Supplement)

    @Query("DELETE FROM supplement WHERE id = :id")
    suspend fun deleteSupplementById(id: Int)

    @Query("DELETE FROM supplement")
    suspend fun deleteAllSupplements()

    // Logs Queries
    @Query("SELECT * FROM supplement_log WHERE profileId = :profileId AND date = :date")
    fun getLogsForDate(profileId: String, date: String): Flow<List<SupplementLog>>

    @Query("SELECT * FROM supplement_log WHERE profileId = :profileId AND date = :date AND supplementId = :supplementId")
    suspend fun getLogsForSupplementOnDate(profileId: String, supplementId: Int, date: String): List<SupplementLog>

    @Query("SELECT * FROM supplement_log WHERE profileId = :profileId ORDER BY date DESC")
    fun getAllLogs(profileId: String): Flow<List<SupplementLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SupplementLog)

    @Query("DELETE FROM supplement_log WHERE supplementId = :supplementId")
    suspend fun deleteLogsForSupplement(supplementId: Int)

    @Query("DELETE FROM supplement_log WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("DELETE FROM supplement_log WHERE supplementId = :supplementId AND date = :date")
    suspend fun deleteLogsForSupplementOnDate(supplementId: Int, date: String)

    @Query("DELETE FROM supplement_log")
    suspend fun deleteAllSupplementLogs()
}
