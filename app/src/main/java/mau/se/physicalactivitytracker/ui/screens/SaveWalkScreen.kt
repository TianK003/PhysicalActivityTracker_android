package mau.se.physicalactivitytracker.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import mau.se.physicalactivitytracker.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import mau.se.physicalactivitytracker.ui.viewmodels.MapViewModel
import mau.se.physicalactivitytracker.ui.viewmodels.MapViewModelFactory

@Composable
fun SaveWalkScreen(
    navController: NavHostController,
    mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val context = LocalContext.current
    var walkName by remember { mutableStateOf("") }
    val elapsedTime by mapViewModel.elapsedTimeMs.collectAsState()
    val stepCount by mapViewModel.stepCount.collectAsState()

    // Handle missing data
    if (elapsedTime == null || stepCount == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Name Input
        OutlinedTextField(
            value = walkName,
            onValueChange = { if (it.length <= 20) walkName = it },
            label = { Text("Walk Name") },
            singleLine = true,
            trailingIcon = { Text("${20 - walkName.length}") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Stats Display
        StatItem(
            icon = R.drawable.ic_time,
            value = elapsedTime?.let { formatElapsedTime(it) } ?: "--:--:--"
        )
        StatItem(
            icon = R.drawable.ic_step,
            value = stepCount?.toString() ?: "0"
        )

        Spacer(Modifier.weight(1f))

        // Save Button
        Button(
            onClick = {
                if (walkName.isNotBlank()) {
                    mapViewModel.saveActivity(walkName)
                    navController.popBackStack()
                    Toast.makeText(context, "Walk saved!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("SAVE", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StatItem(icon: Int, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun formatElapsedTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}