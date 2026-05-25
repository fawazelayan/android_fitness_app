package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplement")
data class Supplement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: String,
    val name: String,
    val type: String, // "Powder", "Capsule", "Liquid", "Gummy"
    val servingUnit: String, // "scoops", "capsules", "ml", "gummies"
    val totalStock: Int,
    val remainingStock: Int,
    val dailyTarget: Int,
    val colorTag: String // Hex color, e.g. "#FF7A5C"
)

@Entity(tableName = "supplement_log")
data class SupplementLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val supplementId: Int,
    val profileId: String,
    val date: String, // "yyyy-MM-dd" format
    val amount: Int
)
