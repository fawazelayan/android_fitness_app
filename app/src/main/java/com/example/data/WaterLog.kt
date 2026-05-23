package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_log")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format "yyyy-MM-dd"
    val profileId: String, // profile indicator
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)
