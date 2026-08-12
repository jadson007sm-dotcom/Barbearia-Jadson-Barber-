package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BarberEntity
import com.example.data.entity.ServiceEntity
import com.example.data.entity.ShopSettingsEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.BookingViewModel
import java.util.*

@Composable
fun AdminPanelScreen(
    adminViewModel: AdminViewModel,
    onCloseAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAuthenticated by adminViewModel.isAuthenticated.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlackAbsolute)
    ) {
        if (!isAuthenticated) {
            AdminAuthLockScreen(
                adminViewModel = adminViewModel,
                onCloseAdmin = onCloseAdmin
            )
        } else {
            AdminDashboardMainContent(
                adminViewModel = adminViewModel,
                onCloseAdmin = onCloseAdmin
            )
        }
    }
}

@Composable
fun AdminAuthLockScreen(
    adminViewModel: AdminViewModel,
    onCloseAdmin: () -> Unit
) {
    val pinInput by adminViewModel.adminPinInput.collectAsState()
    val pinError by adminViewModel.pinError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .border(2.dp, GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "PAINEL ADMINISTRATIVO",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            letterSpacing = 2.sp
        )

        Text(
            text = "JADSON BARBER - Acesso Restrito",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pinInput,
            onValueChange = { adminViewModel.updatePinInput(it) },
            label = { Text("Senha PIN (Padrão: 1234)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            isError = pinError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = DarkBorder,
                focusedLabelColor = GoldPrimary,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (pinError) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "PIN incorreto. Tente '1234'.", color = StatusCancelled, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { adminViewModel.authenticate() },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("ENTRAR NO PAINEL", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onCloseAdmin) {
            Text("Voltar para o App do Cliente", color = TextSecondary)
        }
    }
}

@Composable
fun AdminDashboardMainContent(
    adminViewModel: AdminViewModel,
    onCloseAdmin: () -> Unit
) {
    val selectedTab by adminViewModel.selectedTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Admin Top Bar
        Surface(
            color = DarkSurface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StatusConfirmed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADMIN • JADSON BARBER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                Row {
                    IconButton(onClick = { adminViewModel.logout() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair", tint = StatusCancelled)
                    }
                    IconButton(onClick = onCloseAdmin) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar Admin", tint = TextPrimary)
                    }
                }
            }
        }

        // Navigation Tabs Bar
        val tabs = listOf(
            AdminTab.DASHBOARD to "Dashboard",
            AdminTab.AGENDA to "Agenda",
            AdminTab.BARBERS to "Barbeiros",
            AdminTab.SERVICES to "Serviços",
            AdminTab.CLIENTS to "Clientes",
            AdminTab.SETTINGS to "Ajustes"
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { (tab, label) ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { adminViewModel.selectTab(tab) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = BlackAbsolute,
                        containerColor = DarkSurface,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                AdminTab.DASHBOARD -> AdminDashboardTab(adminViewModel)
                AdminTab.AGENDA -> AdminAgendaTab(adminViewModel)
                AdminTab.BARBERS -> AdminBarbersTab(adminViewModel)
                AdminTab.SERVICES -> AdminServicesTab(adminViewModel)
                AdminTab.CLIENTS -> AdminClientsTab(adminViewModel)
                AdminTab.SETTINGS -> AdminSettingsTab(adminViewModel)
                else -> AdminDashboardTab(adminViewModel)
            }
        }
    }
}

@Composable
fun AdminDashboardTab(adminViewModel: AdminViewModel) {
    val allAppointments by adminViewModel.allAppointments.collectAsState()
    val barberPerf by adminViewModel.barberPerformance.collectAsState()
    val servicePerf by adminViewModel.servicePerformance.collectAsState()

    val todayIso = BookingViewModel.getTodayIso()

    val todayAppts = remember(allAppointments, todayIso) {
        allAppointments.filter { it.dateIso == todayIso }
    }

    val todayRevenue = remember(todayAppts) {
        todayAppts.filter { it.status == "Concluído" || it.status == "Confirmado" }.sumOf { it.totalPrice }
    }

    val totalCompletedRevenue = remember(allAppointments) {
        allAppointments.filter { it.status == "Concluído" || it.status == "Confirmado" }.sumOf { it.totalPrice }
    }

    val totalAppointmentsCount = allAppointments.size
    val ticketMedio = if (totalAppointmentsCount > 0) totalCompletedRevenue / totalAppointmentsCount else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "RESUMO FINANCEIRO & DESEMPENHO", style = MaterialTheme.typography.titleMedium, color = GoldPrimary, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Faturamento Hoje",
                    value = "R$ ${String.format(Locale.getDefault(), "%.2f", todayRevenue)}",
                    subtitle = "${todayAppts.size} agendamento(s)",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Faturamento Total",
                    value = "R$ ${String.format(Locale.getDefault(), "%.2f", totalCompletedRevenue)}",
                    subtitle = "$totalAppointmentsCount cortes totais",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            MetricCard(
                title = "Ticket Médio",
                value = "R$ ${String.format(Locale.getDefault(), "%.2f", ticketMedio)}",
                subtitle = "Média de valor por cliente",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "RANKING DE BARBEIROS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    barberPerf.forEach { b ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = b.barberName, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(text = "${b.appointmentCount} atendimentos", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "R$ ${String.format(Locale.getDefault(), "%.2f", b.totalRevenue)}", color = GoldPrimary, fontWeight = FontWeight.Bold)
                                Text(text = "Comissão: R$ ${String.format(Locale.getDefault(), "%.2f", b.estimatedCommission)}", color = StatusConfirmed, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        HorizontalDivider(color = DarkBorder)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "SERVIÇOS MAIS PROCURADOS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    servicePerf.forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = s.serviceName, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${s.count}x (R$ ${String.format(Locale.getDefault(), "%.2f", s.totalRevenue)})", color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GoldPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = StatusConfirmed)
        }
    }
}

@Composable
fun AdminAgendaTab(adminViewModel: AdminViewModel) {
    val appointments by adminViewModel.filteredAgendaAppointments.collectAsState()
    val selectedDate by adminViewModel.agendaSelectedDate.collectAsState()
    val barbers by adminViewModel.barbers.collectAsState()
    val selectedBarberFilter by adminViewModel.agendaBarberFilter.collectAsState()

    var showWalkInDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "AGENDA DA BARBEARIA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)

            Button(
                onClick = { showWalkInDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Encaixe Presencial", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barber Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedBarberFilter == null,
                    onClick = { adminViewModel.setAgendaBarberFilter(null) },
                    label = { Text("Todos os Barbeiros") }
                )
            }
            items(barbers) { b ->
                FilterChip(
                    selected = selectedBarberFilter == b.id,
                    onClick = { adminViewModel.setAgendaBarberFilter(b.id) },
                    label = { Text(b.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Nenhum agendamento para esta data/filtro.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(appointments, key = { it.id }) { appt ->
                    val statusColor = when (appt.status) {
                        "Confirmado" -> StatusConfirmed
                        "Concluído" -> StatusCompleted
                        "Cancelado" -> StatusCancelled
                        else -> StatusPending
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = appt.timeSlot,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = appt.clientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }

                                Surface(color = statusColor.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp)) {
                                    Text(
                                        text = appt.status,
                                        color = statusColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Barbeiro: ${appt.barberName} • ${appt.serviceName}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text(text = "Tel: ${appt.clientPhone} • R$ ${String.format(Locale.getDefault(), "%.2f", appt.totalPrice)}", color = TextPrimary, style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(10.dp))
                            // Status Quick Actions
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusButton("Confirmar", StatusConfirmed) { adminViewModel.updateAppointmentStatus(appt.id, "Confirmado") }
                                StatusButton("Concluir", StatusCompleted) { adminViewModel.updateAppointmentStatus(appt.id, "Concluído") }
                                StatusButton("Cancelar", StatusCancelled) { adminViewModel.updateAppointmentStatus(appt.id, "Cancelado") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWalkInDialog) {
        WalkInAppointmentDialog(
            barbers = barbers,
            services = adminViewModel.services.collectAsState().value,
            onDismiss = { showWalkInDialog = false },
            onSave = { name, phone, barber, service, time ->
                adminViewModel.addManualAppointment(name, phone, barber, service, selectedDate, time)
                showWalkInDialog = false
            }
        )
    }
}

@Composable
fun StatusButton(text: String, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = BorderStroke(1.dp, color),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 11.sp)
    }
}

@Composable
fun WalkInAppointmentDialog(
    barbers: List<BarberEntity>,
    services: List<ServiceEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, BarberEntity, ServiceEntity, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedBarber by remember { mutableStateOf(barbers.firstOrNull()) }
    var selectedService by remember { mutableStateOf(services.firstOrNull()) }
    var timeSlot by remember { mutableStateOf("14:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Encaixe Presencial", color = GoldPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Cliente") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / WhatsApp") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Horário (ex: 15:30)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val b = selectedBarber ?: return@Button
                    val s = selectedService ?: return@Button
                    onSave(name, phone, b, s, timeSlot)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute)
            ) {
                Text("SALVAR ENCAIXE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AdminBarbersTab(adminViewModel: AdminViewModel) {
    val barbers by adminViewModel.barbers.collectAsState()
    var editingBarber by remember { mutableStateOf<BarberEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "EQUIPE DE BARBEIROS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)

            Button(
                onClick = {
                    editingBarber = BarberEntity(name = "", photoUrl = "", phone = "", specialities = "")
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Novo Barbeiro")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(barbers, key = { it.id }) { barber ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = barber.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = barber.specialities, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(text = "Comissão: ${barber.commissionPercentage.toInt()}% • Expediente: ${barber.workStartTime} - ${barber.workEndTime}", color = GoldAccent, style = MaterialTheme.typography.labelSmall)
                        }

                        Row {
                            IconButton(onClick = {
                                editingBarber = barber
                                showDialog = true
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = GoldPrimary)
                            }
                            IconButton(onClick = { adminViewModel.deleteBarber(barber) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = StatusCancelled)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && editingBarber != null) {
        BarberEditDialog(
            barber = editingBarber!!,
            onDismiss = { showDialog = false },
            onSave = { b ->
                adminViewModel.saveBarber(b)
                showDialog = false
            }
        )
    }
}

@Composable
fun BarberEditDialog(
    barber: BarberEntity,
    onDismiss: () -> Unit,
    onSave: (BarberEntity) -> Unit
) {
    var name by remember { mutableStateOf(barber.name) }
    var phone by remember { mutableStateOf(barber.phone) }
    var specialities by remember { mutableStateOf(barber.specialities) }
    var commission by remember { mutableStateOf(barber.commissionPercentage.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (barber.id == 0L) "Novo Barbeiro" else "Editar Barbeiro", color = GoldPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome Completo") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefone / WhatsApp") })
                OutlinedTextField(value = specialities, onValueChange = { specialities = it }, label = { Text("Especialidades") })
                OutlinedTextField(value = commission, onValueChange = { commission = it }, label = { Text("Comissão (%)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val commVal = commission.toDoubleOrNull() ?: 50.0
                    onSave(barber.copy(name = name, phone = phone, specialities = specialities, commissionPercentage = commVal))
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute)
            ) {
                Text("SALVAR")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun AdminServicesTab(adminViewModel: AdminViewModel) {
    val services by adminViewModel.services.collectAsState()
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "CATÁLOGO DE SERVIÇOS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)

            Button(
                onClick = {
                    editingService = ServiceEntity(name = "", category = "Cabelo", price = 50.0, durationMinutes = 30, description = "")
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Novo Serviço")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(services, key = { it.id }) { service ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = service.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = service.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = "R$ ${String.format(Locale.getDefault(), "%.2f", service.price)} • ${service.durationMinutes} min • ${service.category}",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                editingService = service
                                showDialog = true
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = GoldPrimary)
                            }
                            IconButton(onClick = { adminViewModel.deleteService(service) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = StatusCancelled)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && editingService != null) {
        ServiceEditDialog(
            service = editingService!!,
            onDismiss = { showDialog = false },
            onSave = { s ->
                adminViewModel.saveService(s)
                showDialog = false
            }
        )
    }
}

@Composable
fun ServiceEditDialog(
    service: ServiceEntity,
    onDismiss: () -> Unit,
    onSave: (ServiceEntity) -> Unit
) {
    var name by remember { mutableStateOf(service.name) }
    var category by remember { mutableStateOf(service.category) }
    var price by remember { mutableStateOf(service.price.toString()) }
    var duration by remember { mutableStateOf(service.durationMinutes.toString()) }
    var description by remember { mutableStateOf(service.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (service.id == 0L) "Novo Serviço" else "Editar Serviço", color = GoldPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Serviço") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria (Cabelo, Barba, Combo)") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Preço (R$)") })
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duração (minutos)") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 50.0
                    val d = duration.toIntOrNull() ?: 30
                    onSave(service.copy(name = name, category = category, price = p, durationMinutes = d, description = description))
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute)
            ) {
                Text("SALVAR")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun AdminClientsTab(adminViewModel: AdminViewModel) {
    val clients by adminViewModel.clientSummaries.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "GESTÃO DE CLIENTES", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(clients) { client ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = client.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "${client.visitCount} visita(s)", color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "WhatsApp: ${client.phone}", color = TextSecondary)
                        Text(text = "Total Gasto: R$ ${String.format(Locale.getDefault(), "%.2f", client.totalSpent)}", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(adminViewModel: AdminViewModel) {
    val settings by adminViewModel.shopSettings.collectAsState()

    var shopName by remember(settings) { mutableStateOf(settings?.shopName ?: "JADSON BARBER") }
    var address by remember(settings) { mutableStateOf(settings?.address ?: "") }
    var phone by remember(settings) { mutableStateOf(settings?.phone ?: "") }
    var whatsapp by remember(settings) { mutableStateOf(settings?.whatsappNumber ?: "") }
    var instagram by remember(settings) { mutableStateOf(settings?.instagram ?: "") }
    var cancellation by remember(settings) { mutableStateOf(settings?.cancellationPolicy ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "CONFIGURAÇÕES DA BARBEARIA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldPrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("Nome da Marca") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Endereço Completo") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefone de Contato") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("Número do WhatsApp (com DDD)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = instagram, onValueChange = { instagram = it }, label = { Text("Perfil Instagram") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = cancellation, onValueChange = { cancellation = it }, label = { Text("Regras de Cancelamento") }, modifier = Modifier.fillMaxWidth())
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val current = settings ?: ShopSettingsEntity()
                    adminViewModel.saveShopSettings(
                        current.copy(
                            shopName = shopName,
                            address = address,
                            phone = phone,
                            whatsappNumber = whatsapp,
                            instagram = instagram,
                            cancellationPolicy = cancellation
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("SALVAR CONFIGURAÇÕES", fontWeight = FontWeight.Bold)
            }
        }
    }
}
