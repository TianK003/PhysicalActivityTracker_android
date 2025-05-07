package mau.se.physicalactivitytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mau.se.physicalactivitytracker.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel()
    val currentLanguage by viewModel.languagePreference.collectAsStateWithLifecycle(initialValue = "en")
    val useImperialUnits by viewModel.unitsPreference.collectAsStateWithLifecycle(initialValue = false)

    // Local draft states
    var draftLanguage by remember { mutableStateOf(currentLanguage) }
    var draftUnits by remember { mutableStateOf(useImperialUnits) }

    // Sync drafts when preferences change
    LaunchedEffect(currentLanguage, useImperialUnits) {
        draftLanguage = currentLanguage
        draftUnits = useImperialUnits
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Language Dropdown
        Box(modifier = Modifier.fillMaxWidth(0.8f)) {
            LanguageDropdown(
                currentLanguage = draftLanguage,
                onLanguageSelected = { draftLanguage = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Units Dropdown
        Box(modifier = Modifier.fillMaxWidth(0.8f)) {
            UnitsDropdown(
                useImperial = draftUnits,
                onUnitsSelected = { draftUnits = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.setLanguage(draftLanguage)
                viewModel.setUseImperialUnits(draftUnits)
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                "Save Settings",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = mapOf(
        "en" to "English",
        "sv" to "Svenska"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = languages[currentLanguage] ?: "English",
            onValueChange = {},
            label = { Text("Language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitsDropdown(
    useImperial: Boolean,
    onUnitsSelected: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val units = mapOf(
        false to "Kilometers (km)",
        true to "Miles (mi)"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = units[useImperial] ?: "Kilometers (km)",
            onValueChange = {},
            label = { Text("Measurement Units") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { (isImperial, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onUnitsSelected(isImperial)
                        expanded = false
                    }
                )
            }
        }
    }
}