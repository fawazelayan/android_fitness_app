package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class IntakeRepository(
    private val dao: IntakeDao,
    private val supplementDao: SupplementDao
) {

    val allIntakes: Flow<List<DailyIntake>> = dao.getAllIntakes()

    companion object {
        const val KEY_CREATINE_MAX = "creatine_max_goal"
        const val KEY_PROTEIN_MAX = "protein_max_goal"
        const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        
        const val KEY_WATER_GOAL = "water_daily_goal_liters"
        const val KEY_WATER_REMINDERS_ENABLED = "water_reminders_enabled"
        const val KEY_WATER_REMINDER_HOUR = "water_reminder_hour"
        const val KEY_WATER_REMINDER_MINUTE = "water_reminder_minute"
        
        const val DEFAULT_CREATINE_MAX = 5
        const val DEFAULT_PROTEIN_MAX = 4
        const val DEFAULT_WATER_GOAL = "2.5"
    }

    suspend fun getIntakeForDate(dateStr: String): DailyIntake? {
        return dao.getIntakeForDate(dateStr)
    }

    suspend fun getOrCreateTodayIntake(): DailyIntake {
        val todayStr = getTodayDateString()
        val existing = dao.getIntakeForDate(todayStr)
        if (existing != null) {
            return existing
        }
        
        val cMax = getSettingInt(KEY_CREATINE_MAX, DEFAULT_CREATINE_MAX)
        val pMax = getSettingInt(KEY_PROTEIN_MAX, DEFAULT_PROTEIN_MAX)
        
        val newIntake = DailyIntake(
            date = todayStr,
            creatineCount = 0,
            proteinCount = 0,
            creatineMax = cMax,
            proteinMax = pMax
        )
        dao.insertIntake(newIntake)
        return newIntake
    }

    suspend fun updateIntake(intake: DailyIntake) {
        dao.insertIntake(intake)
    }

    suspend fun setSetting(key: String, value: String) {
        dao.insertSetting(UserSetting(key, value))
    }

    suspend fun setSettings(settings: List<UserSetting>) {
        dao.insertSettings(settings)
    }

    fun getIntakesForProfile(profileId: String): Flow<List<DailyIntake>> {
        return dao.getIntakesForProfile(profileId)
    }

    fun getIntakeFlowForDate(dateStr: String): Flow<DailyIntake?> {
        return dao.getIntakeFlowForDate(dateStr)
    }

    suspend fun getSettingString(key: String, default: String): String {
        return dao.getSetting(key)?.value ?: default
    }

    suspend fun getSettingInt(key: String, default: Int): Int {
        return dao.getSetting(key)?.value?.toIntOrNull() ?: default
    }

    suspend fun getSettingBoolean(key: String, default: Boolean): Boolean {
        return dao.getSetting(key)?.value?.toBoolean() ?: default
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun populateSampleIfEmpty(existingList: List<DailyIntake>) {
        if (existingList.isNotEmpty()) return

        Log.d("IntakeRepository", "Database empty. Initializing default settings.")
        
        // Ensure default settings are initialized with zero or standard maximums, starting with no records logged
        setSetting(KEY_CREATINE_MAX, DEFAULT_CREATINE_MAX.toString())
        setSetting(KEY_PROTEIN_MAX, DEFAULT_PROTEIN_MAX.toString())
        setSetting(KEY_REMINDERS_ENABLED, "false")
    }

    suspend fun clearAllHistory() {
        dao.deleteAllIntakes()
        dao.deleteAllWaterLogs()
        dao.deleteAllFoodLogs()
        supplementDao.deleteAllSupplements()
        supplementDao.deleteAllSupplementLogs()
    }

    // Water Log Operations
    fun getWaterLogs(profileId: String, date: String): Flow<List<WaterLog>> {
        return dao.getWaterLogs(profileId, date)
    }

    suspend fun insertWaterLog(waterLog: WaterLog) {
        dao.insertWaterLog(waterLog)
    }

    suspend fun deleteWaterLog(id: Int) {
        dao.deleteWaterLog(id)
    }

    // Food Log Operations
    fun getFoodLogs(profileId: String, date: String): Flow<List<FoodLog>> {
        return dao.getFoodLogs(profileId, date)
    }

    suspend fun insertFoodLog(foodLog: FoodLog) {
        dao.insertFoodLog(foodLog)
    }

    suspend fun deleteFoodLog(id: Int) {
        dao.deleteFoodLog(id)
    }

    // Supplement Operations
    fun getAllSupplements(profileId: String): Flow<List<Supplement>> {
        return supplementDao.getAllSupplements(profileId)
    }

    suspend fun getSupplementById(id: Int): Supplement? {
        return supplementDao.getSupplementById(id)
    }

    suspend fun insertSupplement(supplement: Supplement) {
        supplementDao.insertSupplement(supplement)
    }

    suspend fun updateSupplement(supplement: Supplement) {
        supplementDao.updateSupplement(supplement)
    }

    suspend fun deleteSupplement(id: Int) {
        supplementDao.deleteLogsForSupplement(id)
        supplementDao.deleteSupplementById(id)
    }

    // Supplement Log Operations
    fun getSupplementLogsForDate(profileId: String, date: String): Flow<List<SupplementLog>> {
        return supplementDao.getLogsForDate(profileId, date)
    }

    fun getAllSupplementLogs(profileId: String): Flow<List<SupplementLog>> {
        return supplementDao.getAllLogs(profileId)
    }

    suspend fun getLogsForSupplementOnDate(profileId: String, supplementId: Int, date: String): List<SupplementLog> {
        return supplementDao.getLogsForSupplementOnDate(profileId, supplementId, date)
    }

    suspend fun insertSupplementLog(log: SupplementLog) {
        supplementDao.insertLog(log)
    }

    suspend fun deleteSupplementLogById(id: Int) {
        supplementDao.deleteLogById(id)
    }

    suspend fun deleteLogsForSupplementOnDate(supplementId: Int, date: String) {
        supplementDao.deleteLogsForSupplementOnDate(supplementId, date)
    }

    suspend fun clearAllSupplementHistory() {
        supplementDao.deleteAllSupplements()
        supplementDao.deleteAllSupplementLogs()
    }
}
