package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "JADSON BARBER",
    val address: String = "Av. Paulista, 1500 - Bela Vista, São Paulo - SP",
    val phone: String = "(11) 99999-8888",
    val whatsappNumber: String = "5511999998888",
    val instagram: String = "@jadsonbarber",
    val cancellationPolicy: String = "Cancelamentos com até 2 horas de antecedência.",
    val openingDays: String = "1,2,3,4,5,6", // 0=Sun to 6=Sat
    val openTime: String = "09:00",
    val closeTime: String = "19:00"
)
