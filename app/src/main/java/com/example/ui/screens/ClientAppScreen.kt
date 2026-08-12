package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.BookingViewModel

enum class ClientBottomTab {
    BOOKING, HISTORY, GALLERY, ABOUT
}

@Composable
fun ClientAppScreen(
    bookingViewModel: BookingViewModel,
    adminViewModel: AdminViewModel
) {
    var activeTab by remember { mutableStateOf(ClientBottomTab.BOOKING) }
    var showAdminOverlay by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ClientTopAppBar(
                onOpenAdmin = { showAdminOverlay = true }
            )
        },
        bottomBar = {
            ClientBottomNavigationBar(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        },
        containerColor = BlackAbsolute
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                ClientBottomTab.BOOKING -> BookingStepScreen(viewModel = bookingViewModel)
                ClientBottomTab.HISTORY -> ClientHistoryScreen(viewModel = bookingViewModel)
                ClientBottomTab.GALLERY -> GalleryScreen()
                ClientBottomTab.ABOUT -> AboutScreen(viewModel = bookingViewModel)
            }

            // Admin Overlay Modal
            if (showAdminOverlay) {
                AdminPanelScreen(
                    adminViewModel = adminViewModel,
                    onCloseAdmin = { showAdminOverlay = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientTopAppBar(onOpenAdmin: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1786570805289),
                        contentDescription = "Logo",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "EXCLUSIVE EXPERIENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "JADSON ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "BARBER",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onOpenAdmin) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Painel Administrativo",
                    tint = GoldPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkSurface
        )
    )
}

@Composable
fun ClientBottomNavigationBar(
    activeTab: ClientBottomTab,
    onTabSelected: (ClientBottomTab) -> Unit
) {
    val items = listOf(
        Triple(ClientBottomTab.BOOKING, "Agenda", Icons.Default.CalendarMonth),
        Triple(ClientBottomTab.HISTORY, "Meus Cortes", Icons.Default.History),
        Triple(ClientBottomTab.GALLERY, "Galeria", Icons.Default.PhotoLibrary),
        Triple(ClientBottomTab.ABOUT, "Barbearia", Icons.Default.Store)
    )

    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        items.forEach { (tab, label, icon) ->
            val isSelected = activeTab == tab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) BlackAbsolute else GoldPrimary
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) GoldPrimary else TextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GoldPrimary
                )
            )
        }
    }
}
