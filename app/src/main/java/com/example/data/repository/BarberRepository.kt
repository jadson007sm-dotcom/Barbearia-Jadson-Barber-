package com.example.data.repository

import com.example.data.dao.AppointmentDao
import com.example.data.dao.BarberDao
import com.example.data.dao.ServiceDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BarberEntity
import com.example.data.entity.ServiceEntity
import com.example.data.entity.ShopSettingsEntity
import kotlinx.coroutines.flow.Flow

class BarberRepository(
    private val barberDao: BarberDao,
    private val serviceDao: ServiceDao,
    private val appointmentDao: AppointmentDao,
    private val shopSettingsDao: ShopSettingsDao
) {
    // Barbers
    val activeBarbers: Flow<List<BarberEntity>> = barberDao.getAllActiveBarbers()
    val allBarbers: Flow<List<BarberEntity>> = barberDao.getAllBarbers()

    suspend fun getBarberById(id: Long) = barberDao.getBarberById(id)
    suspend fun insertBarber(barber: BarberEntity) = barberDao.insertBarber(barber)
    suspend fun updateBarber(barber: BarberEntity) = barberDao.updateBarber(barber)
    suspend fun deleteBarber(barber: BarberEntity) = barberDao.deleteBarber(barber)
    suspend fun deleteBarberById(id: Long) = barberDao.deleteBarberById(id)

    // Services
    val activeServices: Flow<List<ServiceEntity>> = serviceDao.getAllActiveServices()
    val allServices: Flow<List<ServiceEntity>> = serviceDao.getAllServices()

    suspend fun getServiceById(id: Long) = serviceDao.getServiceById(id)
    suspend fun insertService(service: ServiceEntity) = serviceDao.insertService(service)
    suspend fun updateService(service: ServiceEntity) = serviceDao.updateService(service)
    suspend fun deleteService(service: ServiceEntity) = serviceDao.deleteService(service)
    suspend fun deleteServiceById(id: Long) = serviceDao.deleteServiceById(id)

    // Appointments
    val allAppointments: Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()

    fun getAppointmentsForDate(dateIso: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getAppointmentsForDate(dateIso)

    suspend fun getOccupiedAppointments(barberId: Long, dateIso: String): List<AppointmentEntity> =
        appointmentDao.getAppointmentsForBarberAndDate(barberId, dateIso)

    fun getAppointmentsForClientPhone(phone: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getAppointmentsForClientPhone(phone)

    suspend fun insertAppointment(appointment: AppointmentEntity) =
        appointmentDao.insertAppointment(appointment)

    suspend fun updateAppointmentStatus(id: Long, status: String) =
        appointmentDao.updateAppointmentStatus(id, status)

    suspend fun deleteAppointment(appointment: AppointmentEntity) =
        appointmentDao.deleteAppointment(appointment)

    // Settings
    val shopSettings: Flow<ShopSettingsEntity?> = shopSettingsDao.getSettingsFlow()
    suspend fun getSettingsDirect(): ShopSettingsEntity? = shopSettingsDao.getSettings()
    suspend fun saveSettings(settings: ShopSettingsEntity) = shopSettingsDao.saveSettings(settings)
}
