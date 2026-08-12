package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val clientPhone: String,
    val barberId: Long,
    val barberName: String,
    val serviceId: Long,
    val serviceName: String,
    val totalPrice: Double,
    val durationMinutes: Int,
    val dateIso: String, // YYYY-MM-DD
    val timeSlot: String, // HH:mm
    val status: String, // Pendente, Confirmado, Concluído, Cancelado, No-Show
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
