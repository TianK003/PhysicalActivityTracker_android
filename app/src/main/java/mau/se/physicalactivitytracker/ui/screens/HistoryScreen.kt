package mau.se.physicalactivitytracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mau.se.physicalactivitytracker.ui.components.ActivityRecord
import mau.se.physicalactivitytracker.ui.components.DateRangeSelector
import mau.se.physicalactivitytracker.viewmodels.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    val activities by viewModel.activities.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    val currentSortType by viewModel.sortType.collectAsState()
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
                        // Sort Button
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

                        // Date Range Picker (placeholder)
                        DateRangeSelector(
                            startDate = Date(),
                            endDate = Date(),
                            dateFormatter = dateFormatter,
                            onClick = { showPicker = true }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(activities) { activity ->
                ActivityRecord(
                    date = activity.date,
                    distance = activity.distance,
                    steps = activity.steps,
                    duration = activity.duration
                )
            }
        }
    }
}