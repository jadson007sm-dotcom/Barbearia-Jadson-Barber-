package com.example.data.dao

import androidx.room.*
import com.example.data.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY dateIso DESC, timeSlot ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE dateIso = :dateIso AND barberId = :barberId AND status != 'Cancelado'")
    suspend fun getAppointmentsForBarberAndDate(barberId: Long, dateIso: String): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE dateIso = :dateIso AND status != 'Cancelado'")
    fun getAppointmentsForDate(dateIso: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE clientPhone = :phone ORDER BY dateIso DESC, timeSlot DESC")
    fun getAppointmentsForClientPhone(phone: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Long): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun getAppointmentCount(): Int
}
