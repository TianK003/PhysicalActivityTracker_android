package mau.se.physicalactivitytracker.ui.components

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mau.se.physicalactivitytracker.R
import mau.se.physicalactivitytracker.ui.theme.MaterialGreen
import mau.se.physicalactivitytracker.ui.viewmodels.MapViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun StartActivityButton(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel,
    context: Context
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.handlePermissionResult(permissions)
    }

    // Observe permission state
    val permissionState = viewModel.permissionState.collectAsStateWithLifecycle().value

    LaunchedEffect(permissionState) {
        when (val state = permissionState) {
            is MapViewModel.PermissionState.Required -> {
                permissionLauncher.launch(state.permissions.toTypedArray())
            }
            is MapViewModel.PermissionState.Denied -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    FloatingActionButton(
        onClick = { viewModel.startActivity(context) },
        modifier = modifier,
        containerColor = MaterialGreen
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_start_tracking),
            contentDescription = "Start Tracking"
        )
    }
}