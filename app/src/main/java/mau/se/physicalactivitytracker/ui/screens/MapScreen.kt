package mau.se.physicalactivitytracker.ui.screens

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import mau.se.physicalactivitytracker.R
import mau.se.physicalactivitytracker.ui.components.NameDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mau.se.physicalactivitytracker.ui.components.StartActivityButton
import mau.se.physicalactivitytracker.ui.viewmodels.MapViewModel
import mau.se.physicalactivitytracker.ui.viewmodels.MapViewModelFactory
import mau.se.physicalactivitytracker.ui.viewmodels.SettingsViewModel

// Malmo Central Station coordinates - default fallback if gps is not available
private val MALMO_CENTRAL = LatLng(55.609929, 13.0008886)

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val cameraPositionState = rememberCameraPositionState()
    var showGpsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var stopButtonJob by remember { mutableStateOf<Job?>(null) }
    // to draw
    val gpsPoints by mapViewModel.gpsPoints.collectAsState()

    val permissionState by mapViewModel.permissionState.collectAsState()

    // Observe recording state from ViewModel
    val isRecording by mapViewModel.isRecording.collectAsState()

    // Location services
    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Location permissions state
    var locationPermissionsGranted by remember { mutableStateOf(false) }
    // Body sensors permission state (for step counter)
    var bodySensorsPermissionGranted by remember { mutableStateOf(false) }


    // Check GPS status
    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // Combined permission launcher for location and body sensors
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        mapViewModel.handlePermissionResult(permissions)
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionsGranted = fineLocationGranted || coarseLocationGranted
        bodySensorsPermissionGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true

        if (locationPermissionsGranted && !isGpsEnabled()) {
            showGpsDialog = true
        }
    }

    // Check permissions on launch
    LaunchedEffect(permissionState) {
        when (val state = permissionState) {
            is MapViewModel.PermissionState.Required -> {
                permissionLauncher.launch(state.permissions.toTypedArray())
                // Reset the permission state after handling
                mapViewModel.handlePermissionResult(emptyMap())
            }
            else -> {}
        }
    }

    // Handle initial camera position
    LaunchedEffect(locationPermissionsGranted) {
        if (locationPermissionsGranted) {
            try {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnSuccessListener { location ->
                    location?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            15f
                        )
                    } ?: run {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            MALMO_CENTRAL,
                            15f
                        )
                    }
                }.addOnFailureListener {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        MALMO_CENTRAL,
                        15f
                    )
                }
            } catch (e: SecurityException) {
                // Handle exception if permissions revoked after granted
                e.printStackTrace()
                cameraPositionState.position = CameraPosition.fromLatLngZoom(MALMO_CENTRAL, 15f)
            }
        } else {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(MALMO_CENTRAL, 15f)
        }
    }

    // GPS Dialog
    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            title = { Text(stringResource(R.string.gps_required)) },
            text = { Text(stringResource(R.string.enable_gps_message)) },
            confirmButton = {
                Button({
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    showGpsDialog = false
                }) {
                    Text(stringResource(R.string.enable_gps))
                }
            },
            dismissButton = {
                Button({ showGpsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionsGranted
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        ) {
            if (gpsPoints.isNotEmpty()) {
                Polyline(
                    points = gpsPoints.map { LatLng(it.latitude, it.longitude) },
                    color = Color.Red,
                    width = 16f
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart)
        ) {
            // Zoom In Button
            IconButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_zoom_in),
                    contentDescription = "Zoom in",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Zoom Out Button
            IconButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_zoom_out),
                    contentDescription = "Zoom out",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Location Button
            IconButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    if (locationPermissionsGranted) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(it.latitude, it.longitude),
                                                15f
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (e: SecurityException) {
                            // Handle permission issues
                            e.printStackTrace()
                        }
                    } else {
                        val requiredPermissions = mutableListOf<String>()
                        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                        permissionLauncher.launch(requiredPermissions.toTypedArray())
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_my_location),
                    contentDescription = "Current location",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Conditionally display Start or Stop button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            if (isRecording) {
                var isBeingPressed by remember { mutableStateOf(false) }
                val buttonScale by animateFloatAsState(
                    targetValue = if (isBeingPressed) 1.2f else 1f,
                    label = "buttonScale"
                )
                val buttonColor by animateColorAsState(
                    targetValue = if (isBeingPressed) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.errorContainer,
                    label = "buttonColor"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (isBeingPressed) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onErrorContainer,
                    label = "iconTint"
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .clip(CircleShape)
                        .background(buttonColor)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { _ ->
                                    isBeingPressed = true
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    val job = scope.launch {
                                        delay(3000)
                                        mapViewModel.stopActivity()
                                    }
                                    stopButtonJob = job
                                    tryAwaitRelease()
                                    job.cancel()
                                    isBeingPressed = false
                                    if (job.isCancelled) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.hold_to_stop),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = if (isBeingPressed) Color.Black.copy(alpha = 0.2f)
                                else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stop_tracking),
                            contentDescription = "Stop activity",
                            tint = iconTint,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                StartActivityButton(
                    viewModel = mapViewModel,
                    context = context
                )
            }
        }
    }
    NameDialog(mapViewModel, settingsViewModel)
}