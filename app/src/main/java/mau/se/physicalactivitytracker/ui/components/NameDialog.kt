package mau.se.physicalactivitytracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mau.se.physicalactivitytracker.ui.viewmodels.MapViewModel
import mau.se.physicalactivitytracker.R
import mau.se.physicalactivitytracker.data.settings.UserPreferencesRepository
import mau.se.physicalactivitytracker.ui.viewmodels.SettingsViewModel

@Composable
fun NameDialog(
    viewModel: MapViewModel,
    settingsViewModel: SettingsViewModel
) {
    var name by remember { mutableStateOf("") }

    val language by settingsViewModel.languagePreference.collectAsState(initial = UserPreferencesRepository.DEFAULT_LANGUAGE)

    if (viewModel.showNameDialog.collectAsState().value) {
        Dialog(
            onDismissRequest = { /* Prevent dismiss */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(localizedStringResource(R.string.name_your_walk, language), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(localizedStringResource(R.string.walk_name, language)) },
                        singleLine = true
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { viewModel.cancelSaveActivity() }
                        ) { Text(localizedStringResource(R.string.cancel, language)) }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.saveActivity(name) },
                            enabled = name.isNotBlank()
                        ) { Text(localizedStringResource(R.string.save, language)) }
                    }
                }
            }
        }
    }
}