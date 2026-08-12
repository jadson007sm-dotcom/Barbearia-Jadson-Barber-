package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
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

enum class BookingStep {
    DATE, BARBER, TIME, SERVICE, SUMMARY, SUCCESS
}

data class TimeSlotItem(
    val time: String,
    val isAvailable: Boolean,
    val reason: String = ""
)

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = BarberRepository(
        db.barberDao(),
        db.serviceDao(),
        db.appointmentDao(),
        db.shopSettingsDao()
    )

    init {
        // Ensure initial seed data exists if DB was recreated
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.seedDatabase(db)
        }
    }

    // Active Barbers & Services
    val barbers: StateFlow<List<BarberEntity>> = repository.activeBarbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ServiceEntity>> = repository.activeServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<ShopSettingsEntity?> = repository.shopSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Booking Flow State
    private val _currentStep = MutableStateFlow(BookingStep.DATE)
    val currentStep: StateFlow<BookingStep> = _currentStep.asStateFlow()

    private val _selectedDateIso = MutableStateFlow(getTodayIso())
    val selectedDateIso: StateFlow<String> = _selectedDateIso.asStateFlow()

    private val _selectedBarber = MutableStateFlow<BarberEntity?>(null)
    val selectedBarber: StateFlow<BarberEntity?> = _selectedBarber.asStateFlow()

    private val _selectedTimeSlot = MutableStateFlow<String?>(null)
    val selectedTimeSlot: StateFlow<String?> = _selectedTimeSlot.asStateFlow()

    private val _selectedServices = MutableStateFlow<List<ServiceEntity>>(emptyList())
    val selectedServices: StateFlow<List<ServiceEntity>> = _selectedServices.asStateFlow()

    // Client Form
    var clientName = MutableStateFlow("")
    var clientPhone = MutableStateFlow("")
    var clientNotes = MutableStateFlow("")

    // Time Slots calculation
    private val _availableTimeSlots = MutableStateFlow<List<TimeSlotItem>>(emptyList())
    val availableTimeSlots: StateFlow<List<TimeSlotItem>> = _availableTimeSlots.asStateFlow()

    // Last created appointment
    private val _lastCreatedAppointment = MutableStateFlow<AppointmentEntity?>(null)
    val lastCreatedAppointment: StateFlow<AppointmentEntity?> = _lastCreatedAppointment.asStateFlow()

    // Client History
    var historySearchPhone = MutableStateFlow("")
    val clientHistoryAppointments: StateFlow<List<AppointmentEntity>> = historySearchPhone
        .flatMapLatest { phone ->
            if (phone.length >= 8) {
                repository.getAppointmentsForClientPhone(phone)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(dateIso: String) {
        _selectedDateIso.value = dateIso
        _selectedTimeSlot.value = null
        recalculateTimeSlots()
    }

    fun selectBarber(barber: BarberEntity) {
        _selectedBarber.value = barber
        _selectedTimeSlot.value = null
        recalculateTimeSlots()
    }

    fun selectTimeSlot(time: String) {
        _selectedTimeSlot.value = time
    }

    fun toggleService(service: ServiceEntity) {
        val current = _selectedServices.value.toMutableList()
        if (current.any { it.id == service.id }) {
            current.removeAll { it.id == service.id }
        } else {
            current.add(service)
        }
        _selectedServices.value = current
        recalculateTimeSlots()
    }

    fun goToStep(step: BookingStep) {
        _currentStep.value = step
        if (step == BookingStep.TIME) {
            recalculateTimeSlots()
        }
    }

    fun resetBooking() {
        _currentStep.value = BookingStep.DATE
        _selectedDateIso.value = getTodayIso()
        _selectedBarber.value = null
        _selectedTimeSlot.value = null
        _selectedServices.value = emptyList()
        clientName.value = ""
        clientPhone.value = ""
        clientNotes.value = ""
        _lastCreatedAppointment.value = null
    }

    fun recalculateTimeSlots() {
        val barber = _selectedBarber.value ?: return
        val dateIso = _selectedDateIso.value

        viewModelScope.launch(Dispatchers.IO) {
            val occupied = repository.getOccupiedAppointments(barber.id, dateIso)
            val slots = generateSlotsForBarber(barber, dateIso, occupied)
            _availableTimeSlots.value = slots
        }
    }

    private fun generateSlotsForBarber(
        barber: BarberEntity,
        dateIso: String,
        occupiedAppointments: List<AppointmentEntity>
    ): List<TimeSlotItem> {
        val slots = mutableListOf<TimeSlotItem>()

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateSdf = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault())

        val isToday = dateIso == getTodayIso()
        val nowCalendar = Calendar.getInstance()
        val currentMinutes = nowCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nowCalendar.get(Calendar.MINUTE)

        val startMinutes = timeToMinutes(barber.workStartTime)
        val endMinutes = timeToMinutes(barber.workEndTime)
        val lunchStart = timeToMinutes(barber.lunchStartTime)
        val lunchEnd = timeToMinutes(barber.lunchEndTime)

        // Generate 30 min increments
        var minute = startMinutes
        while (minute < endMinutes) {
            val slotTimeStr = minutesToTime(minute)

            var isAvailable = true
            var reason = ""

            // 1. Check Lunch Break
            if (minute >= lunchStart && minute < lunchEnd) {
                isAvailable = false
                reason = "Almoço do Barbeiro"
            }

            // 2. Check Past time if today
            if (isToday && minute <= currentMinutes + 15) {
                isAvailable = false
                reason = "Horário passado"
            }

            // 3. Check occupied appointments
            val isOccupied = occupiedAppointments.any { appt ->
                val apptStart = timeToMinutes(appt.timeSlot)
                val apptEnd = apptStart + appt.durationMinutes
                minute >= apptStart && minute < apptEnd
            }

            if (isOccupied) {
                isAvailable = false
                reason = "Já Ocupado"
            }

            slots.add(TimeSlotItem(time = slotTimeStr, isAvailable = isAvailable, reason = reason))
            minute += 30
        }

        return slots
    }

    fun submitBooking(context: Context) {
        val barber = _selectedBarber.value ?: return
        val dateIso = _selectedDateIso.value
        val timeSlot = _selectedTimeSlot.value ?: return
        val servicesList = _selectedServices.value
        if (servicesList.isEmpty()) return

        val totalDuration = servicesList.sumOf { it.durationMinutes }
        val totalPrice = servicesList.sumOf { it.price }
        val serviceNames = servicesList.joinToString(" + ") { it.name }
        val serviceIds = servicesList.map { it.id }.firstOrNull() ?: 0L

        val appointment = AppointmentEntity(
            clientName = clientName.value.ifBlank { "Cliente" },
            clientPhone = clientPhone.value.ifBlank { "(11) 90000-0000" },
            barberId = barber.id,
            barberName = barber.name,
            serviceId = serviceIds,
            serviceName = serviceNames,
            totalPrice = totalPrice,
            durationMinutes = totalDuration,
            dateIso = dateIso,
            timeSlot = timeSlot,
            status = "Confirmado",
            notes = clientNotes.value
        )

        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertAppointment(appointment)
            val created = appointment.copy(id = id)
            _lastCreatedAppointment.value = created
            _currentStep.value = BookingStep.SUCCESS
        }
    }

    fun openWhatsAppConfirmation(context: Context, appt: AppointmentEntity) {
        val currentSettings = settings.value
        val shopPhone = currentSettings?.whatsappNumber ?: "5511999998888"

        val formattedDate = formatDateDisplay(appt.dateIso)
        val text = """
            *NOVO AGENDAMENTO - JADSON BARBER* ✂️🔥
            
            👤 *Cliente:* ${appt.clientName}
            📞 *Telefone:* ${appt.clientPhone}
            ✂️ *Barbeiro:* ${appt.barberName}
            💈 *Serviço(s):* ${appt.serviceName}
            📅 *Data:* $formattedDate
            ⏰ *Horário:* ${appt.timeSlot}
            💰 *Valor Total:* R$ ${String.format(Locale.getDefault(), "%.2f", appt.totalPrice)}
            
            Confirmado pelo App Jadson Barber!
        """.trimIndent()

        val url = "https://api.whatsapp.com/send?phone=$shopPhone&text=${Uri.encode(text)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAppointment(appointmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAppointmentStatus(appointmentId, "Cancelado")
        }
    }

    private fun timeToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            parts[0].trim().toInt() * 60 + parts[1].trim().toInt()
        } catch (e: Exception) {
            540 // 09:00 default
        }
    }

    private fun minutesToTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    companion object {
        fun getTodayIso(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        fun formatDateDisplay(dateIso: String): String {
            return try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputSdf.parse(dateIso) ?: Date()
                val outputSdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy (EEE)", Locale("pt", "BR"))
                outputSdf.format(date)
            } catch (e: Exception) {
                dateIso
            }
        }
    }
}
