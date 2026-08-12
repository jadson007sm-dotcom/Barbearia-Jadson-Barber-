package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookingViewModel

@Composable
fun AboutScreen(
    viewModel: BookingViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    val shopName = settings?.shopName ?: "JADSON BARBER"
    val address = settings?.address ?: "Av. Paulista, 1500 - Bela Vista, São Paulo - SP"
    val phone = settings?.phone ?: "(11) 99999-8888"
    val whatsapp = settings?.whatsappNumber ?: "5511999998888"
    val instagram = settings?.instagram ?: "@jadsonbarber"
    val cancellationPolicy = settings?.cancellationPolicy ?: "Cancelamentos com até 2 horas de antecedência."

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackAbsolute)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Image
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner_1786570814934),
            contentDescription = "Ambiente Jadson Barber",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = shopName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary
        )
        Text(
            text = "Barbearia de Alto Padrão • Atendimento Executivo Exclusivo",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info Cards
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Endereço", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = address, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        context.startActivity(mapIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackAbsolute),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ABRIR NO GOOGLE MAPS", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Contatos & Redes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Telefone: $phone", color = TextSecondary)
                Text(text = "Instagram: $instagram", color = GoldAccent, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val url = "https://api.whatsapp.com/send?phone=$whatsapp"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FALAR NO WHATSAPP", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Horário de Funcionamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "• Segunda a Sábado: 09:00 - 19:00", color = TextSecondary)
                Text(text = "• Domingos e Feriados: Fechado (Sob agendamento especial)", color = TextSecondary)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Política de Cancelamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = cancellationPolicy, color = TextSecondary)
            }
        }
    }
}
