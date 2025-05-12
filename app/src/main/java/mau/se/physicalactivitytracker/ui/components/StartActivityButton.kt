package mau.se.physicalactivitytracker.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mau.se.physicalactivitytracker.R
import mau.se.physicalactivitytracker.ui.theme.MaterialGreen
import mau.se.physicalactivitytracker.ui.viewmodels.MapViewModel
import android.Manifest

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun StartActivityButton(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel,
    context: Context
) {
    // Define the launcher for the notification permission request
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with starting the activity
            viewModel.startActivity(context)
        } else {
            // Permission denied, inform the user
            Toast.makeText(context, "Notification permission is required to show recording status.", Toast.LENGTH_LONG).show()
        }
    }

    // Observe permission state
    val permissionState = viewModel.permissionState.collectAsStateWithLifecycle().value

    LaunchedEffect(permissionState) {
        when (val state = permissionState) {
            is MapViewModel.PermissionState.Required -> {
                // Launch the permission request
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            is MapViewModel.PermissionState.Denied -> {
                // Show a toast message if permission is denied
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {
                // Handle other states if necessary
            }
        }
    }

    // Floating Action Button to start the activity
    FloatingActionButton(
        onClick = {
            // Check if the permission is already granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with starting the activity
                viewModel.startActivity(context)
            } else {
                // Request the notification permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        },
        modifier = modifier,
        containerColor = MaterialGreen
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_start_tracking),
            contentDescription = "Start Tracking"
        )
    }
}
