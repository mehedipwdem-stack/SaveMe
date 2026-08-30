package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.SosTriggerService
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.FakeCallDialog
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SafetyToolsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RedPrimary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleSosIntent(intent)

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSosIntent(intent)
    }

    private fun handleSosIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_SOS_TRIGGERED", false) == true) {
            viewModel.executeSos("POWER_BUTTON_3X")
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val communityAlerts by viewModel.communityAlerts.collectAsState()
    val alertHistory by viewModel.alertHistory.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isBengali = settings.isBengali

    // Permission Launcher for Location, SMS, Call & Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            viewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearStatusMessage()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.setScreen(AppScreen.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.isEmergencyActive) Icons.Default.Warning else Icons.Default.Home,
                                contentDescription = "Home",
                                tint = if (uiState.isEmergencyActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                text = if (isBengali) "বিপদ সংকেত" else "SOS Home",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == AppScreen.HOME) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedPrimary,
                            selectedTextColor = RedPrimary,
                            indicatorColor = RedPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.COMMUNITY,
                        onClick = { viewModel.setScreen(AppScreen.COMMUNITY) },
                        icon = {
                            Icon(imageVector = Icons.Default.Campaign, contentDescription = "Community")
                        },
                        label = {
                            Text(
                                text = if (isBengali) "কমিউনিটি" else "Community",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == AppScreen.COMMUNITY) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedPrimary,
                            selectedTextColor = RedPrimary,
                            indicatorColor = RedPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_community")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CONTACTS,
                        onClick = { viewModel.setScreen(AppScreen.CONTACTS) },
                        icon = {
                            Icon(imageVector = Icons.Default.ContactPhone, contentDescription = "Contacts")
                        },
                        label = {
                            Text(
                                text = if (isBengali) "কন্ট্যাক্ট" else "Contacts",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == AppScreen.CONTACTS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedPrimary,
                            selectedTextColor = RedPrimary,
                            indicatorColor = RedPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_contacts")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.TOOLS,
                        onClick = { viewModel.setScreen(AppScreen.TOOLS) },
                        icon = {
                            Icon(imageVector = Icons.Default.Security, contentDescription = "Tools")
                        },
                        label = {
                            Text(
                                text = if (isBengali) "সুরক্ষা টুলস" else "Safety Tools",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == AppScreen.TOOLS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedPrimary,
                            selectedTextColor = RedPrimary,
                            indicatorColor = RedPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_tools")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            uiState = uiState,
                            settings = settings,
                            contacts = contacts,
                            communityAlerts = communityAlerts,
                            onSosClick = { viewModel.triggerSosWithCountdown("SOS_BUTTON") },
                            onCancelCountdown = { viewModel.cancelSosCountdown() },
                            onCancelEmergency = { viewModel.cancelActiveEmergency() },
                            onRefreshLocation = { viewModel.refreshLocation() },
                            onCallNumber = { num -> viewModel.call999Emergency(num) },
                            onToggleSiren = { viewModel.toggleSiren() },
                            onToggleStrobe = { viewModel.toggleStrobe() },
                            onStartFakeCall = { viewModel.startFakeCall() },
                            onNavigateToScreen = { screen -> viewModel.setScreen(screen) },
                            onToggleLanguage = { viewModel.toggleLanguage() }
                        )
                    }

                    AppScreen.COMMUNITY -> {
                        CommunityScreen(
                            communityAlerts = communityAlerts,
                            settings = settings,
                            onBroadcastSos = { viewModel.triggerSosWithCountdown("COMMUNITY_BROADCAST") },
                            onRespondToAlert = { alert -> viewModel.respondToCommunityVictim(alert) },
                            onMarkSafe = { alert -> viewModel.markCommunityAlertSafe(alert) },
                            onSimulateAlert = { viewModel.simulateIncomingDistressAlert() }
                        )
                    }

                    AppScreen.CONTACTS -> {
                        ContactsScreen(
                            contacts = contacts,
                            settings = settings,
                            uiState = uiState,
                            onAddContact = { contact -> viewModel.addContact(contact) },
                            onUpdateContact = { contact -> viewModel.updateContact(contact) },
                            onDeleteContact = { contact -> viewModel.deleteContact(contact) },
                            onSetPrimary = { id -> viewModel.setPrimaryContact(id) }
                        )
                    }

                    AppScreen.TOOLS -> {
                        SafetyToolsScreen(
                            settings = settings,
                            uiState = uiState,
                            alertHistory = alertHistory,
                            onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                            onToggleSiren = { viewModel.toggleSiren() },
                            onToggleStrobe = { viewModel.toggleStrobe() },
                            onStartFakeCall = { viewModel.startFakeCall() },
                            onTestPowerTrigger = { viewModel.executeSos("SIMULATED_POWER_BUTTON_3X") }
                        )
                    }
                }
            }
        }

        // Realistic Fake Call Overlay Screen
        if (uiState.isFakeCallActive) {
            FakeCallDialog(
                callerName = if (isBengali) "পুলিশ অফিসার / পুলিশ কন্ট্রোল রুম" else "Emergency Police Officer",
                callerNumber = "999 / +8801711000000",
                isBengali = isBengali,
                onDismiss = { viewModel.endFakeCall() }
            )
        }
    }
}
