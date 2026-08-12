package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookingViewModel
import java.util.*

@Composable
fun ClientHistoryScreen(
    viewModel: BookingViewModel,
    modifier: Modifier = Modifier
) {
    val phoneSearch by viewModel.historySearchPhone.collectAsState()
    val appointments by viewModel.clientHistoryAppointments.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackAbsolute)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Meus Agendamentos",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneSearch,
            onValueChange = { viewModel.historySearchPhone.value = it },
            label = { Text("Digite seu WhatsApp para buscar histórico") },
            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = GoldPrimary) },
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
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

        Spacer(modifier = Modifier.height(16.dp))

        if (phoneSearch.length < 8) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Informe seu número de telefone com DDD para consultar seus cortes e agendamentos futuros.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum agendamento encontrado para o telefone informado.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = appt.serviceName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Surface(
                                    color = statusColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = appt.status,
                                        color = statusColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Barbeiro: ${appt.barberName}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                text = "Data: ${BookingViewModel.formatDateDisplay(appt.dateIso)} às ${appt.timeSlot}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Valor: R$ ${String.format(Locale.getDefault(), "%.2f", appt.totalPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )

                            if (appt.status == "Pendente" || appt.status == "Confirmado") {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { viewModel.cancelAppointment(appt.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled),
                                    border = BorderStroke(1.dp, StatusCancelled),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("CANCELAR AGENDAMENTO")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
