package mau.se.physicalactivitytracker.ui.screens

import HistoryViewModel
import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mau.se.physicalactivitytracker.ui.components.ActivityRecord
import mau.se.physicalactivitytracker.ui.components.DateRangeSelector
import mau.se.physicalactivitytracker.ui.viewmodels.HistoryViewModelFactory
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    navController: NavController
) {
    val activities by viewModel.activities.collectAsState()
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val currentSortType by viewModel.sortType.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clickable { showSortMenu = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Sort by: ${currentSortType.name.lowercase()
                                            .replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                HistoryViewModel.SortType.entries.forEach { sortType ->
                                    DropdownMenuItem(
                                        text = { Text("By ${sortType.name.lowercase()}") },
                                        onClick = {
                                            viewModel.setSortType(sortType)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        DateRangeSelector(
                            startDate = startDate,
                            endDate = endDate,
                            dateFormatter = dateFormatter,
                            onClick = { showPicker = true }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracking data available.\nTry a different time range.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(activities) { activity ->
                    ActivityRecord(
                        name = activity.name,
                        date = activity.date,
                        distance = activity.distance,
                        steps = activity.steps,
                        duration = activity.duration,
                        onMapClick = {
                            navController.navigate("activity_details/${activity.id}")
                        },
                        onDelete = { viewModel.deleteActivity(activity.id) }
                    )
                }
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate.time,
            initialSelectedEndDateMillis = endDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedStartDateMillis?.let { startMillis ->
                            pickerState.selectedEndDateMillis?.let { endMillis ->
                                viewModel.updateDates(
                                    start = Date(startMillis),
                                    end = Date(endMillis)
                                )
                            }
                        }
                        showPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = pickerState,
                title = { Text("Select date range", Modifier.padding(16.dp)) }
            )
        }
    }
}