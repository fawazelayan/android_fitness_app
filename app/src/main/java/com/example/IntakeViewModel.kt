package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyIntake
import com.example.data.IntakeRepository
import com.example.data.WaterLog
import com.example.data.UserSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IntakeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = IntakeRepository(db.intakeDao)

    private val _activeProfile = MutableStateFlow("profile_1")
    val activeProfile: StateFlow<String> = _activeProfile.asStateFlow()

    private val _profile1Name = MutableStateFlow("Me")
    val profile1Name: StateFlow<String> = _profile1Name.asStateFlow()

    private val _profile2Name = MutableStateFlow("Person 2")
    val profile2Name: StateFlow<String> = _profile2Name.asStateFlow()

    // Historical intake listings reactive stream - combined & filtered for the active profile
    private val _intakesHistory = MutableStateFlow<List<DailyIntake>>(emptyList())
    val intakesHistory: StateFlow<List<DailyIntake>> = _intakesHistory.asStateFlow()

    private val _todayIntake = MutableStateFlow<DailyIntake?>(null)
    val todayIntake: StateFlow<DailyIntake?> = _todayIntake.asStateFlow()

    private val _creatineMax = MutableStateFlow(IntakeRepository.DEFAULT_CREATINE_MAX)
    val creatineMax: StateFlow<Int> = _creatineMax.asStateFlow()

    private val _proteinMax = MutableStateFlow(IntakeRepository.DEFAULT_PROTEIN_MAX)
    val proteinMax: StateFlow<Int> = _proteinMax.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(false)
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(20)
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(0)
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Water Intake Tracking States
    private val _waterLogsToday = MutableStateFlow<List<WaterLog>>(emptyList())
    val waterLogsToday: StateFlow<List<WaterLog>> = _waterLogsToday.asStateFlow()

    private val _waterGoal = MutableStateFlow(2.5) // in Liters
    val waterGoal: StateFlow<Double> = _waterGoal.asStateFlow()

    private val _waterRemindersEnabled = MutableStateFlow(false)
    val waterRemindersEnabled: StateFlow<Boolean> = _waterRemindersEnabled.asStateFlow()

    private val _waterReminderHour = MutableStateFlow(10)
    val waterReminderHour: StateFlow<Int> = _waterReminderHour.asStateFlow()

    private val _waterReminderMinute = MutableStateFlow(0)
    val waterReminderMinute: StateFlow<Int> = _waterReminderMinute.asStateFlow()

    // Nutrition & Macronutrient Tracking States
    private val _foodLogsToday = MutableStateFlow<List<com.example.data.FoodLog>>(emptyList())
    val foodLogsToday: StateFlow<List<com.example.data.FoodLog>> = _foodLogsToday.asStateFlow()

    private val _calorieGoal = MutableStateFlow(2000.0)
    val calorieGoal: StateFlow<Double> = _calorieGoal.asStateFlow()

    private val _proteinGoal = MutableStateFlow(150.0)
    val proteinGoal: StateFlow<Double> = _proteinGoal.asStateFlow()

    private val _carbGoal = MutableStateFlow(250.0)
    val carbGoal: StateFlow<Double> = _carbGoal.asStateFlow()

    private val _fatGoal = MutableStateFlow(70.0)
    val fatGoal: StateFlow<Double> = _fatGoal.asStateFlow()

    // Profile physical metrics
    private val _gender = MutableStateFlow("Male")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _age = MutableStateFlow("25")
    val age: StateFlow<String> = _age.asStateFlow()

    private val _height = MutableStateFlow("175")
    val height: StateFlow<String> = _height.asStateFlow()

    private val _weight = MutableStateFlow("70")
    val weight: StateFlow<String> = _weight.asStateFlow()

    private val _activityLevel = MutableStateFlow("Moderately Active")
    val activityLevel: StateFlow<String> = _activityLevel.asStateFlow()

    private val _fitnessGoal = MutableStateFlow("Maintain")
    val fitnessGoal: StateFlow<String> = _fitnessGoal.asStateFlow()

    // AI analysis states
    private val _isAnalyzingLabel = MutableStateFlow(false)
    val isAnalyzingLabel: StateFlow<Boolean> = _isAnalyzingLabel.asStateFlow()

    private val _analysisResult = MutableStateFlow<com.example.data.NutritionLabelResult?>(null)
    val analysisResult: StateFlow<com.example.data.NutritionLabelResult?> = _analysisResult.asStateFlow()

    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Check if DB is empty to populate sample records
            val initialList = repository.allIntakes.first()
            if (initialList.isEmpty()) {
                repository.populateSampleIfEmpty(initialList)
            }

            // Load active profile and names
            val activeProf = repository.getSettingString("active_profile_id", "profile_1")
            _activeProfile.value = activeProf
            _profile1Name.value = repository.getSettingString("profile_1_name", "Me")
            _profile2Name.value = repository.getSettingString("profile_2_name", "Person 2")

            // Load user profile configurations or fallback to default levels
            val cMax = repository.getSettingInt("${activeProf}_${IntakeRepository.KEY_CREATINE_MAX}", IntakeRepository.DEFAULT_CREATINE_MAX)
            val pMax = repository.getSettingInt("${activeProf}_${IntakeRepository.KEY_PROTEIN_MAX}", IntakeRepository.DEFAULT_PROTEIN_MAX)
            val remEnabled = repository.getSettingBoolean(IntakeRepository.KEY_REMINDERS_ENABLED, false)
            val remHour = repository.getSettingInt(IntakeRepository.KEY_REMINDER_HOUR, 20)
            val remMinute = repository.getSettingInt("reminder_minute", 0)
            val dMode = repository.getSettingBoolean("is_dark_mode", false)

            val wGoalStr = repository.getSettingString("${activeProf}_${IntakeRepository.KEY_WATER_GOAL}", IntakeRepository.DEFAULT_WATER_GOAL)
            val wGoal = wGoalStr.toDoubleOrNull() ?: 2.5
            val wRemEnabled = repository.getSettingBoolean(IntakeRepository.KEY_WATER_REMINDERS_ENABLED, false)
            val wRemHour = repository.getSettingInt(IntakeRepository.KEY_WATER_REMINDER_HOUR, 10)
            val wRemMinute = repository.getSettingInt(IntakeRepository.KEY_WATER_REMINDER_MINUTE, 0)

            _creatineMax.value = cMax
            _proteinMax.value = pMax
            _remindersEnabled.value = remEnabled
            _reminderHour.value = remHour
            _reminderMinute.value = remMinute
            _isDarkMode.value = dMode

            _waterGoal.value = wGoal
            _waterRemindersEnabled.value = wRemEnabled
            _waterReminderHour.value = wRemHour
            _waterReminderMinute.value = wRemMinute

            // Load nutrition goals for the active profile
            val calGoal = repository.getSettingInt("${activeProf}_calorie_goal", 2000).toDouble()
            val protGoal = repository.getSettingInt("${activeProf}_protein_goal", 150).toDouble()
            val carbGoal = repository.getSettingInt("${activeProf}_carb_goal", 250).toDouble()
            val fatGoal = repository.getSettingInt("${activeProf}_fat_goal", 70).toDouble()

            _calorieGoal.value = calGoal
            _proteinGoal.value = protGoal
            _carbGoal.value = carbGoal
            _fatGoal.value = fatGoal

            // Load physical profile configurations
            _gender.value = repository.getSettingString("${activeProf}_gender", "Male")
            _age.value = repository.getSettingString("${activeProf}_age", "25")
            _height.value = repository.getSettingString("${activeProf}_height", "175")
            _weight.value = repository.getSettingString("${activeProf}_weight", "70")
            _activityLevel.value = repository.getSettingString("${activeProf}_activity_level", "Moderately Active")
            _fitnessGoal.value = repository.getSettingString("${activeProf}_fitness_goal", "Maintain")

            // Collect today's water logs for the active profile
            var waterLogsJob: kotlinx.coroutines.Job? = null
            launch {
                _activeProfile.collect { profile ->
                    waterLogsJob?.cancel()
                    waterLogsJob = launch {
                        val todayStr = repository.getTodayDateString()
                        repository.getWaterLogs(profile, todayStr).collect { logs ->
                            _waterLogsToday.value = logs
                        }
                    }
                }
            }

            // Collect today's food logs for the active profile
            var foodLogsJob: kotlinx.coroutines.Job? = null
            launch {
                _activeProfile.collect { profile ->
                    foodLogsJob?.cancel()
                    foodLogsJob = launch {
                        val todayStr = repository.getTodayDateString()
                        repository.getFoodLogs(profile, todayStr).collect { logs ->
                            _foodLogsToday.value = logs
                        }
                    }
                }
            }

            // Collect active profile's historical intakes
            var intakesJob: kotlinx.coroutines.Job? = null
            launch {
                _activeProfile.collect { profile ->
                    intakesJob?.cancel()
                    intakesJob = launch {
                        repository.getIntakesForProfile(profile).collect { list ->
                            val cleanedList = list.map { it.copy(date = it.date.removePrefix("${profile}_")) }
                            _intakesHistory.value = cleanedList
                        }
                    }
                }
            }

            // Collect to monitor active today's intake dynamically and automatically
            var todayIntakeJob: kotlinx.coroutines.Job? = null
            launch {
                _activeProfile.collect { profile ->
                    todayIntakeJob?.cancel()
                    todayIntakeJob = launch {
                        val todayStr = repository.getTodayDateString()
                        val dbKey = "${profile}_$todayStr"
                        repository.getIntakeFlowForDate(dbKey).collect { entry ->
                            if (entry == null) {
                                launch(kotlinx.coroutines.Dispatchers.IO) {
                                    val pCMax = repository.getSettingInt("${profile}_${IntakeRepository.KEY_CREATINE_MAX}", IntakeRepository.DEFAULT_CREATINE_MAX)
                                    val pPMax = repository.getSettingInt("${profile}_${IntakeRepository.KEY_PROTEIN_MAX}", IntakeRepository.DEFAULT_PROTEIN_MAX)
                                    val newIntake = DailyIntake(
                                        date = dbKey,
                                        creatineCount = 0,
                                        proteinCount = 0,
                                        creatineMax = pCMax,
                                        proteinMax = pPMax
                                    )
                                    repository.updateIntake(newIntake)
                                }
                            } else {
                                _todayIntake.value = entry
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun getOrCreateTodayIntakeForActiveProfile(): DailyIntake {
        val profile = _activeProfile.value
        val todayStr = repository.getTodayDateString()
        val dbKey = "${profile}_$todayStr"
        val existing = repository.getIntakeForDate(dbKey)
        if (existing != null) {
            return existing
        }
        val cMax = _creatineMax.value
        val pMax = _proteinMax.value
        val newIntake = DailyIntake(
            date = dbKey,
            creatineCount = 0,
            proteinCount = 0,
            creatineMax = cMax,
            proteinMax = pMax
        )
        repository.updateIntake(newIntake)
        return newIntake
    }

    fun incrementCreatine() {
        viewModelScope.launch {
            val current = _todayIntake.value ?: getOrCreateTodayIntakeForActiveProfile()
            val cMax = _creatineMax.value
            
            if (current.creatineCount < cMax) {
                val newCount = current.creatineCount + 1
                val ticked = newCount >= current.creatineMax
                val updated = current.copy(
                    creatineCount = newCount,
                    creatineTicked = ticked,
                    isTicked = ticked || current.proteinTicked
                )
                repository.updateIntake(updated)
                _todayIntake.value = updated
            }
        }
    }

    fun incrementProtein() {
        viewModelScope.launch {
            val current = _todayIntake.value ?: getOrCreateTodayIntakeForActiveProfile()
            val pMax = _proteinMax.value
            
            if (current.proteinCount < pMax) {
                val newCount = current.proteinCount + 1
                val ticked = newCount >= current.proteinMax
                val updated = current.copy(
                    proteinCount = newCount,
                    proteinTicked = ticked,
                    isTicked = current.creatineTicked || ticked
                )
                repository.updateIntake(updated)
                _todayIntake.value = updated
            }
        }
    }

    fun decrementCreatine() {
        viewModelScope.launch {
            val current = _todayIntake.value ?: getOrCreateTodayIntakeForActiveProfile()
            if (current.creatineCount > 0) {
                val newCount = current.creatineCount - 1
                val ticked = newCount >= current.creatineMax
                val updated = current.copy(
                    creatineCount = newCount,
                    creatineTicked = ticked,
                    isTicked = ticked || current.proteinTicked
                )
                repository.updateIntake(updated)
                _todayIntake.value = updated
            }
        }
    }

    fun decrementProtein() {
        viewModelScope.launch {
            val current = _todayIntake.value ?: getOrCreateTodayIntakeForActiveProfile()
            if (current.proteinCount > 0) {
                val newCount = current.proteinCount - 1
                val ticked = newCount >= current.proteinMax
                val updated = current.copy(
                    proteinCount = newCount,
                    proteinTicked = ticked,
                    isTicked = current.creatineTicked || ticked
                )
                repository.updateIntake(updated)
                _todayIntake.value = updated
            }
        }
    }


    fun setCreatineGoal(max: Int) {
        if (max <= 0) return
        viewModelScope.launch {
            val profile = _activeProfile.value
            repository.setSetting("${profile}_${IntakeRepository.KEY_CREATINE_MAX}", max.toString())
            _creatineMax.value = max
            
            // Keep today's local maximum limit aligned
            val current = _todayIntake.value
            if (current != null) {
                val ticked = current.creatineCount >= max
                val updated = current.copy(
                    creatineMax = max,
                    creatineTicked = ticked,
                    isTicked = ticked || current.proteinTicked
                )
                repository.updateIntake(updated)
                _todayIntake.value = updated
            }
        }
    }

    fun setProteinGoal(max: Int) {
        if (max <= 0) return
        viewModelScope.launch {
            val profile = _activeProfile.value
            repository.setSetting("${profile}_${IntakeRepository.KEY_PROTEIN_MAX}", max.toString())
            _proteinMax.value = max
            
            // Keep today's local maximum limit aligned
            val current = _todayIntake.value
            if (current != null) {
                val ticked = current.proteinCount >= max
                val updated = current.copy(
                    proteinMax = max,
                    proteinTicked = ticked,
                    isTicked = current.creatineTicked || ticked
                )
                repository.updateIntake(updated)
                _todayIntake.value = updated
            }
        }
    }

    fun toggleReminders(context: Context) {
        viewModelScope.launch {
            val nextState = !_remindersEnabled.value
            repository.setSetting(IntakeRepository.KEY_REMINDERS_ENABLED, nextState.toString())
            _remindersEnabled.value = nextState
            
            if (nextState) {
                val hour = repository.getSettingInt(IntakeRepository.KEY_REMINDER_HOUR, 20)
                val minute = repository.getSettingInt("reminder_minute", 0)
                ReminderReceiver.scheduleDailyReminder(context.applicationContext, hour, minute)
            } else {
                ReminderReceiver.cancelDailyReminder(context.applicationContext)
            }
        }
    }

    fun changeReminderTime(context: Context, hour: Int, minute: Int) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return
        viewModelScope.launch {
            repository.setSetting(IntakeRepository.KEY_REMINDER_HOUR, hour.toString())
            repository.setSetting("reminder_minute", minute.toString())
            _reminderHour.value = hour
            _reminderMinute.value = minute
            
            // Re-schedule alarm immediately if reminders are enabled
            if (_remindersEnabled.value) {
                ReminderReceiver.scheduleDailyReminder(context.applicationContext, hour, minute)
            }
        }
    }

    fun changeReminderHour(context: Context, hour: Int) {
        if (hour < 0 || hour > 23) return
        viewModelScope.launch {
            repository.setSetting(IntakeRepository.KEY_REMINDER_HOUR, hour.toString())
            _reminderHour.value = hour
            
            // Re-schedule alarm immediately if reminders are enabled
            if (_remindersEnabled.value) {
                ReminderReceiver.scheduleDailyReminder(context.applicationContext, hour, _reminderMinute.value)
            }
        }
    }

    fun toggleDayTicked(dateStr: String) {
        viewModelScope.launch {
            val prefix = _activeProfile.value
            val dbKey = "${prefix}_$dateStr"
            val existing = repository.getIntakeForDate(dbKey)
            if (existing != null) {
                val newTicked = !existing.isTicked
                val updated = existing.copy(
                    creatineCount = if (newTicked) existing.creatineMax else 0,
                    proteinCount = if (newTicked) existing.proteinMax else 0,
                    creatineTicked = newTicked,
                    proteinTicked = newTicked,
                    isTicked = newTicked
                )
                repository.updateIntake(updated)
            } else {
                val cMax = _creatineMax.value
                val pMax = _proteinMax.value
                val newRecord = DailyIntake(
                    date = dbKey,
                    creatineCount = cMax,
                    proteinCount = pMax,
                    creatineMax = cMax,
                    proteinMax = pMax,
                    creatineTicked = true,
                    proteinTicked = true,
                    isTicked = true
                )
                repository.updateIntake(newRecord)
            }
        }
    }

    fun updateLogForDay(dateStr: String, cCount: Int, pCount: Int, cTicked: Boolean, pTicked: Boolean) {
        viewModelScope.launch {
            val prefix = _activeProfile.value
            val dbKey = "${prefix}_$dateStr"
            val existing = repository.getIntakeForDate(dbKey)
            val cMax = _creatineMax.value
            val pMax = _proteinMax.value

            val finalCMax = existing?.creatineMax ?: cMax
            val finalPMax = existing?.proteinMax ?: pMax

            // Do not override user count when ticked
            val finalCCount = cCount
            val finalPCount = pCount

            val finalCTicked = cTicked
            val finalPTicked = pTicked

            if (existing != null) {
                val updated = existing.copy(
                    creatineCount = finalCCount,
                    proteinCount = finalPCount,
                    creatineTicked = finalCTicked,
                    proteinTicked = finalPTicked,
                    isTicked = finalCTicked || finalPTicked
                )
                repository.updateIntake(updated)
            } else {
                val newRecord = DailyIntake(
                    date = dbKey,
                    creatineCount = finalCCount,
                    proteinCount = finalPCount,
                    creatineMax = finalCMax,
                    proteinMax = finalPMax,
                    creatineTicked = finalCTicked,
                    proteinTicked = finalPTicked,
                    isTicked = finalCTicked || finalPTicked
                )
                repository.updateIntake(newRecord)
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val nextState = !_isDarkMode.value
            repository.setSetting("is_dark_mode", nextState.toString())
            _isDarkMode.value = nextState
        }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            repository.setSetting("active_profile_id", profileId)
            _activeProfile.value = profileId
            
            // Reload the goals for this profile:
            val cMax = repository.getSettingInt("${profileId}_${IntakeRepository.KEY_CREATINE_MAX}", IntakeRepository.DEFAULT_CREATINE_MAX)
            val pMax = repository.getSettingInt("${profileId}_${IntakeRepository.KEY_PROTEIN_MAX}", IntakeRepository.DEFAULT_PROTEIN_MAX)
            _creatineMax.value = cMax
            _proteinMax.value = pMax

            val wGoalStr = repository.getSettingString("${profileId}_${IntakeRepository.KEY_WATER_GOAL}", IntakeRepository.DEFAULT_WATER_GOAL)
            _waterGoal.value = wGoalStr.toDoubleOrNull() ?: 2.5

            // Reload nutrition goals:
            val calGoal = repository.getSettingInt("${profileId}_calorie_goal", 2000).toDouble()
            val protGoal = repository.getSettingInt("${profileId}_protein_goal", 150).toDouble()
            val carbGoal = repository.getSettingInt("${profileId}_carb_goal", 250).toDouble()
            val fatGoal = repository.getSettingInt("${profileId}_fat_goal", 70).toDouble()

            _calorieGoal.value = calGoal
            _proteinGoal.value = protGoal
            _carbGoal.value = carbGoal
            _fatGoal.value = fatGoal

            // Reload physical settings
            _gender.value = repository.getSettingString("${profileId}_gender", "Male")
            _age.value = repository.getSettingString("${profileId}_age", "25")
            _height.value = repository.getSettingString("${profileId}_height", "175")
            _weight.value = repository.getSettingString("${profileId}_weight", "70")
            _activityLevel.value = repository.getSettingString("${profileId}_activity_level", "Moderately Active")
            _fitnessGoal.value = repository.getSettingString("${profileId}_fitness_goal", "Maintain")
        }
    }

    fun renameProfile(profileId: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.setSetting("${profileId}_name", newName)
            if (profileId == "profile_1") {
                _profile1Name.value = newName
            } else if (profileId == "profile_2") {
                _profile2Name.value = newName
            }
        }
    }

    fun updatePhysicalProfile(
        gender: String,
        age: String,
        height: String,
        weight: String,
        activityLevel: String,
        fitnessGoal: String
    ) {
        viewModelScope.launch {
            val profileId = _activeProfile.value
            val settings = listOf(
                UserSetting("${profileId}_gender", gender),
                UserSetting("${profileId}_age", age),
                UserSetting("${profileId}_height", height),
                UserSetting("${profileId}_weight", weight),
                UserSetting("${profileId}_activity_level", activityLevel),
                UserSetting("${profileId}_fitness_goal", fitnessGoal)
            )
            repository.setSettings(settings)

            _gender.value = gender
            _age.value = age
            _height.value = height
            _weight.value = weight
            _activityLevel.value = activityLevel
            _fitnessGoal.value = fitnessGoal
        }
    }

    fun applyCalculatedGoals(
        calories: Double,
        protein: Double,
        carbs: Double,
        fats: Double,
        waterLiters: Double
    ) {
        viewModelScope.launch {
            val profileId = _activeProfile.value
            val settings = listOf(
                UserSetting("${profileId}_calorie_goal", calories.toInt().toString()),
                UserSetting("${profileId}_protein_goal", protein.toInt().toString()),
                UserSetting("${profileId}_carb_goal", carbs.toInt().toString()),
                UserSetting("${profileId}_fat_goal", fats.toInt().toString()),
                UserSetting("${profileId}_${IntakeRepository.KEY_WATER_GOAL}", waterLiters.toString())
            )
            repository.setSettings(settings)

            _calorieGoal.value = calories
            _proteinGoal.value = protein
            _carbGoal.value = carbs
            _fatGoal.value = fats
            _waterGoal.value = waterLiters
        }
    }

    // Water Log Operations
    fun addWaterLog(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            val profile = _activeProfile.value
            val todayStr = repository.getTodayDateString()
            val newLog = WaterLog(
                date = todayStr,
                profileId = profile,
                amountMl = amountMl
            )
            repository.insertWaterLog(newLog)
        }
    }

    fun deleteWaterLog(id: Int) {
        viewModelScope.launch {
            repository.deleteWaterLog(id)
        }
    }

    fun setWaterGoal(goalLiters: Double) {
        if (goalLiters <= 0.0) return
        viewModelScope.launch {
            val profile = _activeProfile.value
            repository.setSetting("${profile}_${IntakeRepository.KEY_WATER_GOAL}", goalLiters.toString())
            _waterGoal.value = goalLiters
        }
    }

    fun toggleWaterReminders(context: Context) {
        viewModelScope.launch {
            val nextState = !_waterRemindersEnabled.value
            repository.setSetting(IntakeRepository.KEY_WATER_REMINDERS_ENABLED, nextState.toString())
            _waterRemindersEnabled.value = nextState
            
            if (nextState) {
                val hour = _waterReminderHour.value
                val minute = _waterReminderMinute.value
                ReminderReceiver.scheduleDailyWaterReminder(context.applicationContext, hour, minute)
            } else {
                ReminderReceiver.cancelDailyWaterReminder(context.applicationContext)
            }
        }
    }

    fun changeWaterReminderTime(context: Context, hour: Int, minute: Int) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return
        viewModelScope.launch {
            repository.setSetting(IntakeRepository.KEY_WATER_REMINDER_HOUR, hour.toString())
            repository.setSetting(IntakeRepository.KEY_WATER_REMINDER_MINUTE, minute.toString())
            _waterReminderHour.value = hour
            _waterReminderMinute.value = minute
            
            if (_waterRemindersEnabled.value) {
                ReminderReceiver.scheduleDailyWaterReminder(context.applicationContext, hour, minute)
            }
        }
    }

    // Food Log Operations
    fun addFoodLog(
        name: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fats: Double,
        sodium: Double,
        sugars: Double,
        fibers: Double,
        weightGrams: Double
    ) {
        viewModelScope.launch {
            val profile = _activeProfile.value
            val todayStr = repository.getTodayDateString()
            val newLog = com.example.data.FoodLog(
                date = todayStr,
                profileId = profile,
                foodName = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                sodium = sodium,
                sugars = sugars,
                fibers = fibers,
                weightGrams = weightGrams
            )
            repository.insertFoodLog(newLog)
        }
    }

    fun deleteFoodLog(id: Int) {
        viewModelScope.launch {
            repository.deleteFoodLog(id)
        }
    }

    fun setNutritionGoals(calories: Double, protein: Double, carbs: Double, fats: Double) {
        viewModelScope.launch {
            val profile = _activeProfile.value
            repository.setSetting("${profile}_calorie_goal", calories.toInt().toString())
            repository.setSetting("${profile}_protein_goal", protein.toInt().toString())
            repository.setSetting("${profile}_carb_goal", carbs.toInt().toString())
            repository.setSetting("${profile}_fat_goal", fats.toInt().toString())

            _calorieGoal.value = calories
            _proteinGoal.value = protein
            _carbGoal.value = carbs
            _fatGoal.value = fats
        }
    }

    fun analyzeLabel(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzingLabel.value = true
            _analysisError.value = null
            _analysisResult.value = null
            try {
                val result = com.example.data.GeminiService.analyzeNutritionLabel(bitmap)
                if (result != null) {
                    _analysisResult.value = result
                } else {
                    _analysisError.value = "Failed to analyze image. Verify your Gemini API key in the Secrets panel or try again."
                }
            } catch (e: Exception) {
                _analysisError.value = "An error occurred: ${e.message}"
            } finally {
                _isAnalyzingLabel.value = false
            }
        }
    }

    fun clearAnalysis() {
        _analysisResult.value = null
        _analysisError.value = null
        _isAnalyzingLabel.value = false
    }

    fun simulateLabel(
        foodName: String,
        caloriesPer100g: Double,
        proteinPer100g: Double,
        carbsPer100g: Double,
        fatsPer100g: Double,
        sodiumPer100g: Double,
        sugarsPer100g: Double,
        fibersPer100g: Double
    ) {
        _isAnalyzingLabel.value = true
        _analysisError.value = null
        _analysisResult.value = null
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Realistic loading delay!
            _analysisResult.value = com.example.data.NutritionLabelResult(
                foodName = foodName,
                caloriesPer100g = caloriesPer100g,
                proteinPer100g = proteinPer100g,
                carbsPer100g = carbsPer100g,
                fatsPer100g = fatsPer100g,
                sodiumPer100g = sodiumPer100g,
                sugarsPer100g = sugarsPer100g,
                fibersPer100g = fibersPer100g
            )
            _isAnalyzingLabel.value = false
        }
    }
}

