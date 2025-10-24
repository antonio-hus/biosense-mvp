package com.biosense.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.biosense.app.health.FakeHealthConnectManager
import com.biosense.app.health.HealthConnectManager
import com.biosense.app.ui.components.GradientBackground
import com.biosense.app.ui.screens.*
import com.biosense.app.ui.theme.BiosenseTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {

    private val healthConnectManager by lazy {
        HealthConnectManager.getInstance(this)
    }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissionLauncher =
            registerForActivityResult(healthConnectManager.requestPermissionActivityContract) { grantedPermissions ->
                if (grantedPermissions.isNotEmpty()) {
                    Log.d("HealthConnect", "All permissions granted! Health Connect is ready.")
                } else {
                    Log.w("HealthConnect", "Permissions not granted.")
                }
            }

        setupHealthConnect()

        setContent {
            BiosenseTheme {
                MainContent()
            }
        }
    }

    private fun setupHealthConnect() {
        lifecycleScope.launch {
            when (healthConnectManager.getSdkStatus(this@MainActivity)) {
                HealthConnectClient.SDK_AVAILABLE -> {
                    if (!healthConnectManager.hasAllPermissions(HealthConnectManager.PERMISSIONS)) {
                        requestPermissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                    } else {
                        Log.d(
                            "HealthConnect",
                            "Permissions already granted. Health Connect is ready."
                        )
                    }
                }

                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    Log.w("HealthConnect", "Health Connect needs to be updated.")
                    val installIntent =
                        healthConnectManager.getInstallHealthConnectIntent(this@MainActivity)
                    startActivity(installIntent)
                }

                HealthConnectClient.SDK_UNAVAILABLE -> {
                    Log.e("HealthConnect", "Health Connect is not available on this device.")
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()

    GradientBackground {
        NavHost(
            navController = navController,
            startDestination = "today"
        ) {
            composable("today") {
                TodayScreen(
                    currentRoute = "today",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = {
                        navController.navigate("account")
                    }
                )
            }
            composable("trends") {
                TrendsScreen(
                    currentRoute = "trends",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = {
                        navController.navigate("account")
                    }
                )
            }
            composable("chat") {
                ChatScreen(
                    currentRoute = "chat",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = {
                        navController.navigate("account")
                    }
                )
            }
            composable("search") {
                SearchScreen(
                    currentRoute = "search",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = {
                        navController.navigate("account")
                    }
                )
            }
            composable("account") {
                AccountScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    BiosenseTheme {
        MainContent()
    }
}
