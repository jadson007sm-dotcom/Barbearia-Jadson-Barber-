package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BarberEntity
import com.example.data.entity.ServiceEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.data.repository.BarberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ClientSummaryItem(
    val name: String,
    val phone: String,
    val visitCount: Int,
    val totalSpent: Double,
    val lastVisitDate: String
)

data class BarberPerformanceItem(
    val barberName: String,
    val appointmentCount: Int,
    val totalRevenue: Double,
    val estimatedCommission: Double
)

data class ServicePerformanceItem(
    val serviceName: String,
    val count: Int,
    val totalRevenue: Double
)

enum class AdminTab {
    DASHBOARD, AGENDA, BARBERS, SERVICES, SCHEDULE, CLIENTS, SETTINGS
}

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = BarberRepository(
        db.barberDao(),
        db.serviceDao(),
        db.appointmentDao(),
        db.shopSettingsDao()
    )

    // Auth state
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _adminPinInput = MutableStateFlow("")
    val adminPinInput: StateFlow<String> = _adminPinInput.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    // Navigation Tab
    private val _selectedTab = MutableStateFlow(AdminTab.DASHBOARD)
    val selectedTab: StateFlow<AdminTab> = _selectedTab.asStateFlow()

    // Data Flows
    val allAppointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val barbers: StateFlow<List<BarberEntity>> = repository.allBarbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ServiceEntity>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shopSettings: StateFlow<ShopSettingsEntity?> = repository.shopSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Agenda Date Filter
    private val _agendaSelectedDate = MutableStateFlow(BookingViewModel.getTodayIso())
    val agendaSelectedDate: StateFlow<String> = _agendaSelectedDate.asStateFlow()

    private val _agendaBarberFilter = MutableStateFlow<Long?>(null) // null = all barbers
    val agendaBarberFilter: StateFlow<Long?> = _agendaBarberFilter.asStateFlow()

    // Filtered Appointments for Agenda
    val filteredAgendaAppointments: StateFlow<List<AppointmentEntity>> = combine(
        allAppointments,
        _agendaSelectedDate,
        _agendaBarberFilter
    ) { list, dateIso, barberId ->
        list.filter { appt ->
            val matchDate = appt.dateIso == dateIso
            val matchBarber = barberId == null || appt.barberId == barberId
            matchDate && matchBarber
        }.sortedBy { it.timeSlot }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique Client List
    val clientSummaries: StateFlow<List<ClientSummaryItem>> = allAppointments
        .map { appointments ->
            appointments.groupBy { it.clientPhone }
                .map { (phone, clientAppts) ->
                    val name = clientAppts.firstOrNull()?.clientName ?: "Cliente"
                    val validAppts = clientAppts.filter { it.status == "Concluído" || it.status == "Confirmado" }
                    val totalSpent = validAppts.sumOf { it.totalPrice }
                    val lastVisit = clientAppts.maxOfOrNull { it.dateIso } ?: ""
                    ClientSummaryItem(
                        name = name,
                        phone = phone,
                        visitCount = clientAppts.size,
                        totalSpent = totalSpent,
                        lastVisitDate = lastVisit
                    )
                }.sortedByDescending { it.visitCount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Performance Rankings
    val barberPerformance: StateFlow<List<BarberPerformanceItem>> = combine(allAppointments, barbers) { appts, barberList ->
        barberList.map { barber ->
            val bAppts = appts.filter { it.barberId == barber.id && (it.status == "Concluído" || it.status == "Confirmado") }
            val totalRev = bAppts.sumOf { it.totalPrice }
            val comm = totalRev * (barber.commissionPercentage / 100.0)
            BarberPerformanceItem(
                barberName = barber.name,
                appointmentCount = bAppts.size,
                totalRevenue = totalRev,
                estimatedCommission = comm
            )
        }.sortedByDescending { it.totalRevenue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val servicePerformance: StateFlow<List<ServicePerformanceItem>> = allAppointments
        .map { appts ->
            appts.filter { it.status == "Concluído" || it.status == "Confirmado" }
                .groupBy { it.serviceName }
                .map { (serviceName, sAppts) ->
                    ServicePerformanceItem(
                        serviceName = serviceName,
                        count = sAppts.size,
                        totalRevenue = sAppts.sumOf { it.totalPrice }
                    )
                }.sortedByDescending { it.count }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updatePinInput(pin: String) {
        _adminPinInput.value = pin
        _pinError.value = false
    }

    fun authenticate() {
        if (_adminPinInput.value == "1234" || _adminPinInput.value == "0000" || _adminPinInput.value == "jadson") {
            _isAuthenticated.value = true
            _pinError.value = false
        } else {
            _pinError.value = true
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _adminPinInput.value = ""
    }

    fun selectTab(tab: AdminTab) {
        _selectedTab.value = tab
    }

    fun setAgendaDate(dateIso: String) {
        _agendaSelectedDate.value = dateIso
    }

    fun setAgendaBarberFilter(barberId: Long?) {
        _agendaBarberFilter.value = barberId
    }

    fun updateAppointmentStatus(id: Long, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAppointmentStatus(id, status)
        }
    }

    // Walk-in fitting (Encaixe Manual)
    fun addManualAppointment(
        clientName: String,
        clientPhone: String,
        barber: BarberEntity,
        service: ServiceEntity,
        dateIso: String,
        timeSlot: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val appt = AppointmentEntity(
                clientName = clientName.ifBlank { "Cliente Presencial" },
                clientPhone = clientPhone.ifBlank { "(11) 90000-0000" },
                barberId = barber.id,
                barberName = barber.name,
                serviceId = service.id,
                serviceName = service.name,
                totalPrice = service.price,
                durationMinutes = service.durationMinutes,
                dateIso = dateIso,
                timeSlot = timeSlot,
                status = "Confirmado",
                notes = "Encaixe Presencial"
            )
            repository.insertAppointment(appt)
        }
    }

    // Barber CRUD
    fun saveBarber(barber: BarberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (barber.id == 0L) {
                repository.insertBarber(barber)
            } else {
                repository.updateBarber(barber)
            }
        }
    }

    fun deleteBarber(barber: BarberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBarber(barber)
        }
    }

    // Service CRUD
    fun saveService(service: ServiceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (service.id == 0L) {
                repository.insertService(service)
            } else {
                repository.updateService(service)
            }
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteService(service)
        }
    }

    // Settings update
    fun saveShopSettings(settings: ShopSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings)
        }
    }
}
