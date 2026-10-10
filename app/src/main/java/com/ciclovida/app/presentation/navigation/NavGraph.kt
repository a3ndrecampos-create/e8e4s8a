package com.ciclovida.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.ciclovida.app.presentation.home.HomeScreen
import com.ciclovida.app.presentation.insights.InsightsScreen
import com.ciclovida.app.presentation.logentry.LogEntryScreen
import com.ciclovida.app.presentation.security.SecurityGateScreen
import com.ciclovida.app.presentation.settings.SettingsScreen

sealed class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object SecurityGate : Destination("security_gate", "", Icons.Default.Lock)
    data object Home : Destination("home", "Início", Icons.Default.Home)
    data object LogEntry : Destination("log_entry", "Registrar", Icons.Default.Edit)
    data object Insights : Destination("insights", "Insights", Icons.Default.Info)
    data object Settings : Destination("settings", "Ajustes", Icons.Default.Settings)
}

private val bottomBarDestinations = listOf(Destination.Home, Destination.LogEntry, Destination.Insights, Destination.Settings)

@Composable
fun CicloVidaNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val showBottomBar = bottomBarDestinations.any { currentRoute?.hierarchy?.any { h -> h.route == it.route } == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.SecurityGate.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Destination.SecurityGate.route) {
                SecurityGateScreen(
                    onAuthenticated = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(Destination.SecurityGate.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Destination.Home.route) {
                HomeScreen(onGoToLogEntry = { navController.navigate(Destination.LogEntry.route) })
            }
            composable(Destination.LogEntry.route) { LogEntryScreen() }
            composable(Destination.Insights.route) { InsightsScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}
