package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Cabelo, Barba, Combo, Estética
    val price: Double,
    val durationMinutes: Int,
    val description: String,
    val isActive: Boolean = true
)
