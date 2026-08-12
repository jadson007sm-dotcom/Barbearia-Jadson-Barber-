package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barbers")
data class BarberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUrl: String,
    val phone: String,
    val specialities: String,
    val commissionPercentage: Double = 50.0,
    val workStartTime: String = "09:00",
    val workEndTime: String = "19:00",
    val lunchStartTime: String = "12:00",
    val lunchEndTime: String = "13:00",
    val workingDays: String = "1,2,3,4,5,6", // 0=Sun, 1=Mon, ..., 6=Sat
    val isActive: Boolean = true
)
