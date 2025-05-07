package mau.se.physicalactivitytracker.ui.navigation

import mau.se.physicalactivitytracker.R

enum class BottomNavItem(
    val route: String,
    val iconRes: Int,
    val contentDescription: String
) {
    Map("map", R.drawable.ic_map, "Map"),
    History("history", R.drawable.ic_history, "History"),
    Settings("settings", R.drawable.ic_settings, "Settings");

    companion object {
        val items: List<BottomNavItem> = entries.toList()
    }
}