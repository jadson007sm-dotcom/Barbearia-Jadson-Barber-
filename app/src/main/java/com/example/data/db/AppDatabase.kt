package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppointmentDao
import com.example.data.dao.BarberDao
import com.example.data.dao.ServiceDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BarberEntity
import com.example.data.entity.ServiceEntity
import com.example.data.entity.ShopSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(
    entities = [
        BarberEntity::class,
        ServiceEntity::class,
        AppointmentEntity::class,
        ShopSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun barberDao(): BarberDao
    abstract fun serviceDao(): ServiceDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun shopSettingsDao(): ShopSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jadson_barber_db"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(database: AppDatabase) {
            val barberDao = database.barberDao()
            val serviceDao = database.serviceDao()
            val settingsDao = database.shopSettingsDao()
            val appointmentDao = database.appointmentDao()

            if (barberDao.getBarberCount() == 0) {
                barberDao.insertBarber(
                    BarberEntity(
                        name = "Jadson Silva",
                        photoUrl = "",
                        phone = "(11) 98888-1111",
                        specialities = "Master Barber • Pigmentação • Barba com Toalha Quente",
                        commissionPercentage = 60.0,
                        workStartTime = "09:00",
                        workEndTime = "19:00",
                        lunchStartTime = "12:00",
                        lunchEndTime = "13:00",
                        workingDays = "1,2,3,4,5,6"
                    )
                )
                barberDao.insertBarber(
                    BarberEntity(
                        name = "Mateus Oliveira",
                        photoUrl = "",
                        phone = "(11) 98888-2222",
                        specialities = "Especialista em Degradê Navalhado & Freestyle",
                        commissionPercentage = 50.0,
                        workStartTime = "09:00",
                        workEndTime = "19:00",
                        lunchStartTime = "13:00",
                        lunchEndTime = "14:00",
                        workingDays = "1,2,3,4,5,6"
                    )
                )
                barberDao.insertBarber(
                    BarberEntity(
                        name = "Lucas Santos",
                        photoUrl = "",
                        phone = "(11) 98888-3333",
                        specialities = "Barboterapia Imperial • Visagismo & Sobrancelha",
                        commissionPercentage = 50.0,
                        workStartTime = "10:00",
                        workEndTime = "20:00",
                        lunchStartTime = "14:00",
                        lunchEndTime = "15:00",
                        workingDays = "1,2,3,4,5,6"
                    )
                )
            }

            if (serviceDao.getServiceCount() == 0) {
                serviceDao.insertService(
                    ServiceEntity(
                        name = "Corte Masculino Premium",
                        category = "Cabelo",
                        price = 60.00,
                        durationMinutes = 30,
                        description = "Corte moderno com lavagem especial, visagismo e finalização com pomada matificante."
                    )
                )
                serviceDao.insertService(
                    ServiceEntity(
                        name = "Barba Imperial",
                        category = "Barba",
                        price = 50.00,
                        durationMinutes = 30,
                        description = "Toalha quente, alinhamento preciso na navalha, hidratação com óleos nobres e pós-barba."
                    )
                )
                serviceDao.insertService(
                    ServiceEntity(
                        name = "Combo Master (Corte + Barba)",
                        category = "Combo",
                        price = 100.00,
                        durationMinutes = 60,
                        description = "O atendimento completo de alto padrão. Corte premium + barboterapia com toalha quente."
                    )
                )
                serviceDao.insertService(
                    ServiceEntity(
                        name = "Sobrancelha na Navalha",
                        category = "Estética",
                        price = 25.00,
                        durationMinutes = 15,
                        description = "Design masculino limpo e alinhamento anatômico na navalha."
                    )
                )
                serviceDao.insertService(
                    ServiceEntity(
                        name = "Pigmentação de Barba ou Cabelo",
                        category = "Estética",
                        price = 45.00,
                        durationMinutes = 30,
                        description = "Preenchimento de falhas com tinta natural trazendo acabamento de alta definição."
                    )
                )
            }

            if (settingsDao.getSettings() == null) {
                settingsDao.saveSettings(
                    ShopSettingsEntity(
                        id = 1,
                        shopName = "JADSON BARBER",
                        address = "Av. Paulista, 1500 - Bela Vista, São Paulo - SP",
                        phone = "(11) 99999-8888",
                        whatsappNumber = "5511999998888",
                        instagram = "@jadsonbarber",
                        cancellationPolicy = "Cancelamentos permitidos com até 2 horas de antecedência.",
                        openingDays = "1,2,3,4,5,6", // Mon-Sat
                        openTime = "09:00",
                        closeTime = "19:00"
                    )
                )
            }

            if (appointmentDao.getAppointmentCount() == 0) {
                val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                appointmentDao.insertAppointment(
                    AppointmentEntity(
                        clientName = "Carlos Eduardo",
                        clientPhone = "(11) 97777-1111",
                        barberId = 1,
                        barberName = "Jadson Silva",
                        serviceId = 3,
                        serviceName = "Combo Master (Corte + Barba)",
                        totalPrice = 100.0,
                        durationMinutes = 60,
                        dateIso = todayIso,
                        timeSlot = "10:00",
                        status = "Confirmado",
                        notes = "Cliente VIP"
                    )
                )
                appointmentDao.insertAppointment(
                    AppointmentEntity(
                        clientName = "Rafael Lima",
                        clientPhone = "(11) 97777-2222",
                        barberId = 2,
                        barberName = "Mateus Oliveira",
                        serviceId = 1,
                        serviceName = "Corte Masculino Premium",
                        totalPrice = 60.0,
                        durationMinutes = 30,
                        dateIso = todayIso,
                        timeSlot = "11:30",
                        status = "Pendente"
                    )
                )
                appointmentDao.insertAppointment(
                    AppointmentEntity(
                        clientName = "Gabriel Souza",
                        clientPhone = "(11) 97777-3333",
                        barberId = 1,
                        barberName = "Jadson Silva",
                        serviceId = 2,
                        serviceName = "Barba Imperial",
                        totalPrice = 50.0,
                        durationMinutes = 30,
                        dateIso = todayIso,
                        timeSlot = "15:00",
                        status = "Concluído"
                    )
                )
            }
        }
    }
}
