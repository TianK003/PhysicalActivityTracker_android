package mau.se.physicalactivitytracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mau.se.physicalactivitytracker.ui.screens.HistoryScreen
import mau.se.physicalactivitytracker.ui.screens.MapScreen
import mau.se.physicalactivitytracker.ui.screens.SaveWalkScreen
import mau.se.physicalactivitytracker.ui.screens.SettingsScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = BottomNavItem.Map.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(BottomNavItem.Map.route) { MapScreen(navController = navController) }
        composable(BottomNavItem.History.route) { HistoryScreen() }
        composable(BottomNavItem.Settings.route) { SettingsScreen() }
        composable("save_walk") { SaveWalkScreen(navController = navController)}
    }
}