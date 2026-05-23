package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_intake")
data class DailyIntake(
    @PrimaryKey val date: String, // "yyyy-MM-dd" format
    val creatineCount: Int,
    val proteinCount: Int,
    val creatineMax: Int,
    val proteinMax: Int,
    val creatineTicked: Boolean = false,
    val proteinTicked: Boolean = false,
    val isTicked: Boolean = false
)

@Entity(tableName = "user_settings")
data class UserSetting(
    @PrimaryKey val key: String,
    val value: String
)
