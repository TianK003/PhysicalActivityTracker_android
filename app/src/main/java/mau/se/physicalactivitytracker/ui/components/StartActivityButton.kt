package mau.se.physicalactivitytracker.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import mau.se.physicalactivitytracker.R

@Composable
fun AddButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_start_tracking),
            contentDescription = "Start Tracking"
        )
    }
}