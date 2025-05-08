package mau.se.physicalactivitytracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mau.se.physicalactivitytracker.ui.screens.HistoryScreen
import mau.se.physicalactivitytracker.ui.screens.MapScreen
import mau.se.physicalactivitytracker.ui.screens.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = BottomNavItem.Map.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(BottomNavItem.Map.route) { MapScreen() }
        composable(BottomNavItem.History.route) { HistoryScreen() }
        composable(BottomNavItem.Settings.route) { SettingsScreen() }
    }
}