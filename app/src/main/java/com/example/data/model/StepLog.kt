package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_logs")
data class StepLog(
    @PrimaryKey val date: String, // Format: yyyy-MM-dd
    val steps: Int,
    val goal: Int
)
