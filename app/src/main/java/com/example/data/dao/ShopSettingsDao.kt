package com.example.data.dao

import androidx.room.*
import com.example.data.entity.ShopSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopSettingsDao {
    @Query("SELECT * FROM shop_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<ShopSettingsEntity?>

    @Query("SELECT * FROM shop_settings WHERE id = 1")
    suspend fun getSettings(): ShopSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ShopSettingsEntity)
}
