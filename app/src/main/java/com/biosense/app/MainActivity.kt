package com.biosense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.biosense.app.service.health.HealthConnectManager
import kotlinx.coroutines.launch
import com.biosense.app.ui.components.GradientBackground
import com.biosense.app.ui.screens.*
import com.biosense.app.ui.theme.BiosenseTheme
import com.biosense.app.viewmodel.TodayViewModel
import com.biosense.app.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {

    private val healthConnectManager by lazy {
        HealthConnectManager.getInstance(this)
    }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Set<String>>
    private var permissionResult by mutableStateOf(false)
    private var connectionCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissionLauncher =
            registerForActivityResult(healthConnectManager.requestPermissionActivityContract) { grantedPermissions ->
                val allGranted = grantedPermissions.containsAll(HealthConnectManager.PERMISSIONS)
                permissionResult = allGranted
                
                if (allGranted) {
                    connectionCallback?.invoke()
                    connectionCallback = null
                }
            }


        setContent {
            BiosenseTheme {
                MainContent(
                    onRequestHealthPermissions = { callback ->
                        connectionCallback = callback
                        requestHealthPermissions()
                    },
                    permissionGranted = permissionResult
                )
            }
        }
    }

    private fun requestHealthPermissions() {
        lifecycleScope.launch {
            try {
                val sdkStatus = healthConnectManager.getSdkStatus(this@MainActivity)
                
                when (sdkStatus) {
                    HealthConnectClient.SDK_AVAILABLE -> {
                        requestPermissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                    }
                    HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                        val intent = healthConnectManager.getInstallHealthConnectIntent(this@MainActivity)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

@Composable
fun MainContent(
    onRequestHealthPermissions: (callback: () -> Unit) -> Unit = { _ -> },
    permissionGranted: Boolean = false
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    val currentUser by userViewModel.currentUser
    val isUserCreated by userViewModel.isUserCreated
    val todayViewModel: TodayViewModel = viewModel()

    LaunchedEffect(Unit) {
        userViewModel.initialize(context)
    }


    val startDestination = if (isUserCreated && currentUser.name.isNotEmpty()) "loading" else "main"

    GradientBackground {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("loading") {
                LoadingScreen(
                    userName = currentUser.name.ifEmpty { "User" },
                    onLoadingComplete = {
                        navController.navigate("today") {
                            popUpTo("loading") { inclusive = true }
                        }
                    }
                )
            }
            composable("main") {
                MainScreen(
                    onCreateAccount = {
                        navController.navigate("watch_connection")
                    }
                )
            }
            composable("watch_connection") {
                WatchConnectionScreen(
                    onContinue = {
                        navController.navigate("create_account")
                    },
                    onRequestPermissions = { callback ->
                        onRequestHealthPermissions(callback)
                    },
                    permissionGranted = permissionGranted
                )
            }
            composable("create_account") {
                CreateAccountScreen(
                    onComplete = {
                        navController.navigate("loading") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    userViewModel = userViewModel
                )
            }
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
                    },
                    viewModel = todayViewModel
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
                    },
                    todayViewModel = todayViewModel
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
                    },
                    viewModel = todayViewModel
                )
            }
            composable("account") {
                AccountScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    userViewModel = userViewModel
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
