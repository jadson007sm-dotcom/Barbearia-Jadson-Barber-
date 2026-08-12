package com.example.data.dao

import androidx.room.*
import com.example.data.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE isActive = 1 ORDER BY category ASC, name ASC")
    fun getAllActiveServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services ORDER BY category ASC, name ASC")
    fun getAllServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getServiceById(id: Long): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteServiceById(id: Long)

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServiceCount(): Int
}
