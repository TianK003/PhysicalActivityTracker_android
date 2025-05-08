package mau.se.physicalactivitytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mau.se.physicalactivitytracker.ui.components.BottomNavigationBar
import mau.se.physicalactivitytracker.ui.navigation.AppNavHost
import mau.se.physicalactivitytracker.ui.navigation.BottomNavItem
import mau.se.physicalactivitytracker.ui.theme.PhysicalActivityTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhysicalActivityTrackerTheme {
                ActivityTrackerApp()
            }
        }
    }
}

@Composable
fun ActivityTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedTab = when (currentDestination?.route) {
        BottomNavItem.Map.route -> BottomNavItem.Map
        BottomNavItem.History.route -> BottomNavItem.History
        BottomNavItem.Settings.route -> BottomNavItem.Settings
        else -> null
    }

    Scaffold(
        bottomBar = {
            selectedTab?.let {
                BottomNavigationBar(selectedTab) { newTab ->
                    if (newTab.route != currentDestination?.route) {
                        navController.navigate(newTab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = BottomNavItem.Map.route,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PhysicalActivityTrackerTheme {
        ActivityTrackerApp()
    }
}