package mau.se.physicalactivitytracker.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import mau.se.physicalactivitytracker.R
import mau.se.physicalactivitytracker.ui.theme.MaterialGreen

@Composable
fun StartActivityButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialGreen
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_start_tracking),
            contentDescription = "Start Tracking"
        )
    }
}