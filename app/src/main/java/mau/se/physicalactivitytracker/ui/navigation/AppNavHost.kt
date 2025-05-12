package mau.se.physicalactivitytracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mau.se.physicalactivitytracker.ui.screens.ActivityDetailsScreen
import mau.se.physicalactivitytracker.ui.screens.HistoryScreen
import mau.se.physicalactivitytracker.ui.screens.MapScreen
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
        composable(BottomNavItem.Map.route) { MapScreen() }
        composable(BottomNavItem.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(BottomNavItem.Settings.route) { SettingsScreen() }
        composable(
            "activity_details/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.LongType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getLong("activityId") ?: 0L
            ActivityDetailsScreen(
                activityId = activityId,
                onBack = { navController.popBackStack() },
                onBackButtonClick = { navController.popBackStack() }
            )
        }
    }
}