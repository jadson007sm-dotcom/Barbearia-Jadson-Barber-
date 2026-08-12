package com.example.data.dao

import androidx.room.*
import com.example.data.entity.BarberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarberDao {
    @Query("SELECT * FROM barbers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveBarbers(): Flow<List<BarberEntity>>

    @Query("SELECT * FROM barbers ORDER BY id ASC")
    fun getAllBarbers(): Flow<List<BarberEntity>>

    @Query("SELECT * FROM barbers WHERE id = :id")
    suspend fun getBarberById(id: Long): BarberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarber(barber: BarberEntity): Long

    @Update
    suspend fun updateBarber(barber: BarberEntity)

    @Delete
    suspend fun deleteBarber(barber: BarberEntity)

    @Query("DELETE FROM barbers WHERE id = :id")
    suspend fun deleteBarberById(id: Long)

    @Query("SELECT COUNT(*) FROM barbers")
    suspend fun getBarberCount(): Int
}
