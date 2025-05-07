package mau.se.physicalactivitytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import mau.se.physicalactivitytracker.ui.navigation.BottomNavItem

@Composable
fun BottomNavigationBar(
    selected: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp,
        modifier = Modifier.height(64.dp)
    ) {
        BottomNavItem.items.forEach { item ->
            val isSelected = selected == item

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable { onItemSelected(item) }
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.contentDescription,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}