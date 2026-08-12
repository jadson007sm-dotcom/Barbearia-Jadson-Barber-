package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BarberEntity
import com.example.data.entity.ServiceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookingStep
import com.example.ui.viewmodel.BookingViewModel
import com.example.ui.viewmodel.TimeSlotItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingStepScreen(
    viewModel: BookingViewModel,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val selectedDate by viewModel.selectedDateIso.collectAsState()
    val selectedBarber by viewModel.selectedBarber.collectAsState()
    val selectedTimeSlot by viewModel.selectedTimeSlot.collectAsState()
    val selectedServices by viewModel.selectedServices.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackAbsolute)
    ) {
        // Step Indicator Header
        if (currentStep != BookingStep.SUCCESS) {
            BookingProgressHeader(
                currentStep = currentStep,
                onStepClick = { step ->
                    if (step.ordinal < currentStep.ordinal) {
                        viewModel.goToStep(step)
                    }
                }
            )
        }

        // Step Content Switcher
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                BookingStep.DATE -> DateSelectionStep(
                    selectedDateIso = selectedDate,
                    onDateSelected = { dateIso ->
                        viewModel.selectDate(dateIso)
                        viewModel.goToStep(BookingStep.BARBER)
                    }
                )
                BookingStep.BARBER -> BarberSelectionStep(
                    viewModel = viewModel,
                    selectedBarber = selectedBarber,
                    onBarberSelected = { barber ->
                        viewModel.selectBarber(barber)
                        viewModel.goToStep(BookingStep.TIME)
                    },
                    onBack = { viewModel.goToStep(BookingStep.DATE) }
                )
                BookingStep.TIME -> TimeSelectionStep(
                    viewModel = viewModel,
                    selectedTime = selectedTimeSlot,
                    onTimeSelected = { time ->
                        viewModel.selectTimeSlot(time)
                        viewModel.goToStep(BookingStep.SERVICE)
                    },
                    onBack = { viewModel.goToStep(BookingStep.BARBER) }
                )
                BookingStep.SERVICE -> ServiceSelectionStep(
                    viewModel = viewModel,
                    selectedServices = selectedServices,
                    onServiceToggle = { service -> viewModel.toggleService(service) },
                    onContinue = { viewModel.goToStep(BookingStep.SUMMARY) },
                    onBack = { viewModel.goToStep(BookingStep.TIME) }
                )
                BookingStep.SUMMARY -> SummaryConfirmationStep(
                    viewModel = viewModel,
                    onConfirm = { viewModel.submitBooking(context) },
                    onBack = { viewModel.goToStep(BookingStep.SERVICE) }
                )
                BookingStep.SUCCESS -> SuccessStep(
                    viewModel = viewModel,
                    onNewBooking = { viewModel.resetBooking() }
                )
            }
        }
    }
}

@Composable
fun BookingProgressHeader(
    currentStep: BookingStep,
    onStepClick: (BookingStep) -> Unit
) {
    val steps = listOf(
        BookingStep.DATE to "Data",
        BookingStep.BARBER to "Barbeiro",
        BookingStep.TIME to "Horário",
        BookingStep.SERVICE to "Serviços",
        BookingStep.SUMMARY to "Resumo"
    )

    Surface(
        color = DarkSurface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp)
        ) {
            // Segmented Step Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                steps.forEach { (step, _) ->
                    val isVisited = step.ordinal <= currentStep.ordinal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (isVisited) GoldPrimary else DarkBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEach { (step, label) ->
                    val isCompleted = step.ordinal < currentStep.ordinal
                    val isCurrent = step == currentStep

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = when {
                            isCurrent -> GoldPrimary
                            isCompleted -> TextPrimary
                            else -> TextSecondary
                        },
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clickable(enabled = isCompleted) { onStepClick(step) }
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DateSelectionStep(
    selectedDateIso: String,
    onDateSelected: (String) -> Unit
) {
    var calendarMonthOffset by remember { mutableIntStateOf(0) }

    val calendar = remember(calendarMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, calendarMonthOffset)
        }
    }

    val currentMonthName = remember(calendar) {
        SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(calendar.time)
            .replaceFirstChar { it.uppercase() }
    }

    val daysInMonth = remember(calendar) {
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

        val list = mutableListOf<DateItem?>()
        // Padding for previous month days
        for (i in 1 until firstDayOfWeek) {
            list.add(null)
        }

        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)

        val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (day in 1..maxDays) {
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val isPast = tempCal.before(todayCal)
            val dateIso = sdfIso.format(tempCal.time)
            val isSelected = dateIso == selectedDateIso
            val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)

            list.add(
                DateItem(
                    dayNumber = day,
                    dateIso = dateIso,
                    isPast = isPast,
                    isSelected = isSelected,
                    isSunday = dayOfWeek == Calendar.SUNDAY
                )
            )
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "PASSO 1 DE 5",
                style = MaterialTheme.typography.labelMedium,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Escolha a Data do Atendimento",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (calendarMonthOffset > 0) calendarMonthOffset-- },
                            enabled = calendarMonthOffset > 0
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Mês Anterior",
                                tint = if (calendarMonthOffset > 0) GoldPrimary else TextSecondary.copy(alpha = 0.3f)
                            )
                        }

                        Text(
                            text = currentMonthName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )

                        IconButton(
                            onClick = { if (calendarMonthOffset < 2) calendarMonthOffset++ },
                            enabled = calendarMonthOffset < 2
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Próximo Mês",
                                tint = if (calendarMonthOffset < 2) GoldPrimary else TextSecondary.copy(alpha = 0.3f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days of week header
                    val daysHeader = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysHeader.forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Grid
                    val rows = daysInMonth.chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                ) {
                                    if (item != null) {
                                        val bg = when {
                                            item.isSelected -> GoldPrimary
                                            item.isPast -> Color.Transparent
                                            else -> DarkSurface
                                        }

                                        val textColor = when {
                                            item.isSelected -> BlackAbsolute
                                            item.isPast -> TextSecondary.copy(alpha = 0.3f)
                                            else -> TextPrimary
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bg)
                                                .border(
                                                    width = if (item.isSelected) 0.dp else 1.dp,
                                                    color = if (item.isPast) Color.Transparent else DarkBorder,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable(enabled = !item.isPast) {
                                                    onDateSelected(item.dateIso)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${item.dayNumber}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }

                            // Fill remaining empty cells in last row
                            for (i in week.size until 7) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Atendimento de Segunda a Sábado das 09:00 às 19:00. Selecione uma data disponível para prosseguir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

private data class DateItem(
    val dayNumber: Int,
    val dateIso: String,
    val isPast: Boolean,
    val isSelected: Boolean,
    val isSunday: Boolean
)

@Composable
fun BarberSelectionStep(
    viewModel: BookingViewModel,
    selectedBarber: BarberEntity?,
    onBarberSelected: (BarberEntity) -> Unit,
    onBack: () -> Unit
) {
    val barbers by viewModel.barbers.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "PASSO 2 DE 5",
            style = MaterialTheme.typography.labelMedium,
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Escolha o Barbeiro",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(barbers, key = { it.id }) { barber ->
                val isSelected = selectedBarber?.id == barber.id

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GoldContainer else DarkSurfaceVariant
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) GoldPrimary else DarkBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBarberSelected(barber) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(1.5.dp, GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = barber.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = barber.specialities,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(StatusConfirmed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Disponível • Expediente ${barber.workStartTime} às ${barber.workEndTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusConfirmed
                                )
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { onBarberSelected(barber) },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
            border = BorderStroke(1.dp, GoldPrimary)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Voltar para Data")
        }
    }
}

@Composable
fun TimeSelectionStep(
    viewModel: BookingViewModel,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val slots by viewModel.availableTimeSlots.collectAsState()
    val selectedBarber by viewModel.selectedBarber.collectAsState()
    val selectedDate by viewModel.selectedDateIso.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "PASSO 3 DE 5",
            style = MaterialTheme.typography.labelMedium,
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Escolha o Horário",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${selectedBarber?.name ?: "Barbeiro"} • ${BookingViewModel.formatDateDisplay(selectedDate)}",
            style = MaterialTheme.typography.bodyMedium,
            color = GoldAccent
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (slots.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(slots) { item ->
                    val isSelected = selectedTime == item.time

                    val containerBg = when {
                        isSelected -> GoldPrimary
                        item.isAvailable -> DarkSurfaceVariant
                        else -> DarkSurface
                    }

                    val textColor = when {
                        isSelected -> BlackAbsolute
                        item.isAvailable -> TextPrimary
                        else -> TextSecondary.copy(alpha = 0.4f)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = containerBg),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) GoldPrimary else if (item.isAvailable) DarkBorder else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = item.isAvailable) {
                                onTimeSelected(item.time)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.time,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            if (!item.isAvailable) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.reason,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Text("Voltar")
            }
        }
    }
}

@Composable
fun ServiceSelectionStep(
    viewModel: BookingViewModel,
    selectedServices: List<ServiceEntity>,
    onServiceToggle: (ServiceEntity) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val services by viewModel.services.collectAsState()
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = remember(services) {
        listOf("Todos") + services.map { it.category }.distinct()
    }

    val filteredServices = remember(services, selectedCategory) {
        if (selectedCategory == "Todos") services
        else services.filter { it.category == selectedCategory }
    }

    val totalPrice = selectedServices.sumOf { it.price }
    val totalDuration = selectedServices.sumOf { it.durationMinutes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "PASSO 4 DE 5",
            style = MaterialTheme.typography.labelMedium,
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Escolha o(s) Serviço(s)",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSel = cat == selectedCategory
                FilterChip(
                    selected = isSel,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = BlackAbsolute,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredServices, key = { it.id }) { service ->
                val isSelected = selectedServices.any { it.id == service.id }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GoldContainer else DarkSurfaceVariant
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) GoldPrimary else DarkBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onServiceToggle(service) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onServiceToggle(service) },
                            colors = CheckboxDefaults.colors(checkedColor = GoldPrimary, checkmarkColor = BlackAbsolute)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = service.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = service.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${service.durationMinutes} min",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "R$ ${String.format(Locale.getDefault(), "%.2f", service.price)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total Footer Bar
        if (selectedServices.isNotEmpty()) {
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedServices.size} serviço(s) selecionado(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Total: R$ ${String.format(Locale.getDefault(), "%.2f", totalPrice)} • $totalDuration min",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute)
                    ) {
                        Text("Avançar", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Text("Voltar ao Horário")
            }
        }
    }
}

@Composable
fun SummaryConfirmationStep(
    viewModel: BookingViewModel,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDateIso.collectAsState()
    val selectedBarber by viewModel.selectedBarber.collectAsState()
    val selectedTimeSlot by viewModel.selectedTimeSlot.collectAsState()
    val selectedServices by viewModel.selectedServices.collectAsState()

    val clientName by viewModel.clientName.collectAsState()
    val clientPhone by viewModel.clientPhone.collectAsState()
    val clientNotes by viewModel.clientNotes.collectAsState()

    val totalPrice = selectedServices.sumOf { it.price }
    val totalDuration = selectedServices.sumOf { it.durationMinutes }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "PASSO 5 DE 5",
                style = MaterialTheme.typography.labelMedium,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Resumo e Dados do Agendamento",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, GoldPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RESUMO DO ATENDIMENTO",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SummaryRow(label = "Barbearia", value = "JADSON BARBER")
                    SummaryRow(label = "Barbeiro", value = selectedBarber?.name ?: "-")
                    SummaryRow(label = "Data", value = BookingViewModel.formatDateDisplay(selectedDate))
                    SummaryRow(label = "Horário", value = selectedTimeSlot ?: "-")

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DarkBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Serviços Selecionados:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    selectedServices.forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "• ${s.name}", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "R$ ${String.format(Locale.getDefault(), "%.2f", s.price)}",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DarkBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Duração Estimada:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "$totalDuration min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VALOR TOTAL:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "R$ ${String.format(Locale.getDefault(), "%.2f", totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Form Section
        item {
            Text(
                text = "Seus Dados para Confirmação",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = clientName,
                onValueChange = { viewModel.clientName.value = it },
                label = { Text("Seu Nome Completo") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GoldPrimary) },
                singleLine = true,
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = clientPhone,
                onValueChange = { viewModel.clientPhone.value = it },
                label = { Text("Seu WhatsApp / Telefone") },
                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = GoldPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = clientNotes,
                onValueChange = { viewModel.clientNotes.value = it },
                label = { Text("Observações (Opcional)") },
                leadingIcon = { Icon(imageVector = Icons.Default.Notes, contentDescription = null, tint = GoldPrimary) },
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                enabled = clientName.isNotBlank() && clientPhone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = BlackAbsolute,
                    disabledContainerColor = DarkSurfaceVariant,
                    disabledContentColor = TextSecondary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CONFIRMAR AGENDAMENTO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Text("Voltar para Serviços")
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SuccessStep(
    viewModel: BookingViewModel,
    onNewBooking: () -> Unit
) {
    val appt by viewModel.lastCreatedAppointment.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(GoldPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = BlackAbsolute,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "AGENDAMENTO REALIZADO!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Seu horário foi reservado com sucesso na JADSON BARBER.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        appt?.let { a ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, GoldPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Detalhes do Ticket", style = MaterialTheme.typography.labelLarge, color = GoldAccent)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Cliente: ${a.clientName}", color = TextPrimary)
                    Text(text = "Barbeiro: ${a.barberName}", color = TextPrimary)
                    Text(text = "Serviço: ${a.serviceName}", color = TextPrimary)
                    Text(text = "Data/Hora: ${BookingViewModel.formatDateDisplay(a.dateIso)} às ${a.timeSlot}", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    Text(text = "Total: R$ ${String.format(Locale.getDefault(), "%.2f", a.totalPrice)}", color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.openWhatsAppConfirmation(context, a) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ENVIAR CONFIRMAÇÃO VIA WHATSAPP", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNewBooking,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
            border = BorderStroke(1.dp, GoldPrimary)
        ) {
            Text("Fazer Novo Agendamento")
        }
    }
}
