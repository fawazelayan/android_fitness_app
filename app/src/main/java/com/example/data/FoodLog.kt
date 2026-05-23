package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_log")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format "yyyy-MM-dd"
    val profileId: String, // profile indicator
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val sodium: Double, // in mg
    val sugars: Double, // in g
    val fibers: Double, // in g
    val weightGrams: Double,
    val timestamp: Long = System.currentTimeMillis()
)
