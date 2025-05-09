package mau.se.physicalactivitytracker.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import mau.se.physicalactivitytracker.R // Make sure this import is correct

/**
 * A FloatingActionButton Composable for stopping an activity.
 *
 * @param modifier Modifier for this Composable.
 * @param onClick Lambda to be invoked when the button is clicked.
 */
@Composable
fun StopActivityButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.errorContainer // Using error color for stop
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_stop_tracking),
            contentDescription = "Stop Tracking",
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
