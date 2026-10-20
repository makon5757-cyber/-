package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ScreenState
import com.example.ui.TajSendViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.InstructionsScreen
import com.example.ui.screens.ReceiveScreen
import com.example.ui.screens.SendScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.TajSendTheme

class MainActivity : ComponentActivity() {

    private var tajSendViewModel: TajSendViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TajSendTheme(darkTheme = true) {
                val vm: TajSendViewModel = viewModel()
                tajSendViewModel = vm

                handleSharedIntent(intent, vm)

                TajSendApp(viewModel = vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        tajSendViewModel?.let { handleSharedIntent(intent, it) }
    }

    private fun handleSharedIntent(intent: Intent?, vm: TajSendViewModel) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                }
                if (uri != null) {
                    vm.addCustomUris(listOf(uri))
                    vm.navigateTo(ScreenState.SEND)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                }
                if (!uris.isNullOrEmpty()) {
                    vm.addCustomUris(uris)
                    vm.navigateTo(ScreenState.SEND)
                }
            }
        }
    }
}

@Composable
fun TajSendApp(viewModel: TajSendViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = CyberBackground,
        bottomBar = {
            TajSendBottomNavigation(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "screen_crossfade"
            ) { screen ->
                when (screen) {
                    ScreenState.DASHBOARD -> HomeScreen(viewModel = viewModel)
                    ScreenState.SEND -> SendScreen(viewModel = viewModel)
                    ScreenState.RECEIVE -> ReceiveScreen(viewModel = viewModel)
                    ScreenState.HISTORY -> HistoryScreen(viewModel = viewModel)
                    ScreenState.INSTRUCTIONS -> InstructionsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TajSendBottomNavigation(
    currentScreen: ScreenState,
    onScreenSelected: (ScreenState) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar"),
        containerColor = CyberSurface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == ScreenState.DASHBOARD,
            onClick = { onScreenSelected(ScreenState.DASHBOARD) },
            icon = {
                Icon(
                    if (currentScreen == ScreenState.DASHBOARD) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Главная"
                )
            },
            label = { Text("Главная", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldNeon,
                selectedTextColor = EmeraldNeon,
                indicatorColor = EmeraldNeon.copy(alpha = 0.15f),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentScreen == ScreenState.SEND,
            onClick = { onScreenSelected(ScreenState.SEND) },
            icon = {
                Icon(
                    if (currentScreen == ScreenState.SEND) Icons.AutoMirrored.Filled.Send else Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Отправить"
                )
            },
            label = { Text("Отправить", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldNeon,
                selectedTextColor = EmeraldNeon,
                indicatorColor = EmeraldNeon.copy(alpha = 0.15f),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_send")
        )

        NavigationBarItem(
            selected = currentScreen == ScreenState.RECEIVE,
            onClick = { onScreenSelected(ScreenState.RECEIVE) },
            icon = {
                Icon(
                    if (currentScreen == ScreenState.RECEIVE) Icons.Default.CallReceived else Icons.Outlined.CallReceived,
                    contentDescription = "Получить"
                )
            },
            label = { Text("Получить", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanNeon,
                selectedTextColor = CyanNeon,
                indicatorColor = CyanNeon.copy(alpha = 0.15f),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_receive")
        )

        NavigationBarItem(
            selected = currentScreen == ScreenState.HISTORY,
            onClick = { onScreenSelected(ScreenState.HISTORY) },
            icon = {
                Icon(
                    if (currentScreen == ScreenState.HISTORY) Icons.Default.History else Icons.Outlined.History,
                    contentDescription = "Журнал"
                )
            },
            label = { Text("Журнал", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanNeon,
                selectedTextColor = CyanNeon,
                indicatorColor = CyanNeon.copy(alpha = 0.15f),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_history")
        )

        NavigationBarItem(
            selected = currentScreen == ScreenState.INSTRUCTIONS,
            onClick = { onScreenSelected(ScreenState.INSTRUCTIONS) },
            icon = {
                Icon(
                    if (currentScreen == ScreenState.INSTRUCTIONS) Icons.Default.Info else Icons.Outlined.Info,
                    contentDescription = "Справка"
                )
            },
            label = { Text("Справка", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldNeon,
                selectedTextColor = EmeraldNeon,
                indicatorColor = EmeraldNeon.copy(alpha = 0.15f),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_info")
        )
    }
}
