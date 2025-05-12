package mau.se.physicalactivitytracker.ui.screens

import android.app.Application
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import mau.se.physicalactivitytracker.R
import mau.se.physicalactivitytracker.ui.viewmodels.ActivityDetailsViewModel

@Composable
fun ActivityDetailsScreen(
    activityId: Long,
    viewModel: ActivityDetailsViewModel = viewModel(
        factory = ActivityDetailsViewModel.provideFactory(
            LocalContext.current.applicationContext as Application,
            activityId
        )
    ),
    onBack: () -> Unit,
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val gpsPoints by viewModel.gpsPoints.collectAsState()
    val activityDetails by viewModel.activityDetails.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(gpsPoints) {
        if (gpsPoints.isNotEmpty()) {
            val bounds = LatLngBounds.Builder()
            gpsPoints.forEach { point ->
                bounds.include(LatLng(point.latitude, point.longitude))
            }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_go_back),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true
            )
        ) {
            if (gpsPoints.isNotEmpty()) {
                // Polyline
                Polyline(
                    points = gpsPoints.map { LatLng(it.latitude, it.longitude) },
                    color = Color.Red,
                    width = 8f
                )

                // Start marker
                Marker(
                    state = rememberMarkerState(
                        position = LatLng(
                            gpsPoints.first().latitude,
                            gpsPoints.first().longitude
                        )
                    ),
                    title = "Start",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )

                // Finish marker
                Marker(
                    state = rememberMarkerState(
                        position = LatLng(
                            gpsPoints.last().latitude,
                            gpsPoints.last().longitude
                        )
                    ),
                    title = "Finish",
                )
            }
        }

        // Details Card
        activityDetails?.let { details ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = details.name,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(Modifier.height(8.dp))

                    // Date row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                                .format(details.date),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Steps and duration row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_step),
                                contentDescription = "Steps",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("${details.steps}")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_time),
                                contentDescription = "Duration",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(details.duration)
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(48.dp),
            shape = CircleShape,
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            IconButton(
                onClick = onBackButtonClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_go_back),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}