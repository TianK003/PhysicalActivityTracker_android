package mau.se.physicalactivitytracker.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AddButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Add your click handler here
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_add),
            contentDescription = "Add Activity"
        )
    }
}