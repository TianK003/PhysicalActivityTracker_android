package mau.se.physicalactivitytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mau.se.physicalactivitytracker.ui.viewmodels.SettingsViewModel
import mau.se.physicalactivitytracker.R
import mau.se.physicalactivitytracker.ui.components.localizedStringResource

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

        Text(
            text = localizedStringResource(R.string.settings, currentLanguage),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                onUnitsSelected = { draftUnits = it },
                currentLanguage = currentLanguage
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
                text = localizedStringResource(R.string.save, currentLanguage),
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
        "en" to localizedStringResource(R.string.english, currentLanguage),
        "sv" to localizedStringResource(R.string.swedish, currentLanguage)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = languages[currentLanguage] ?: "English",
            onValueChange = {},
            label = { Text(localizedStringResource(R.string.language, currentLanguage)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(
                type = MenuAnchorType.PrimaryNotEditable,
                enabled = true)
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
    onUnitsSelected: (Boolean) -> Unit,
    currentLanguage: String
) {
    var expanded by remember { mutableStateOf(false) }
    val units = mapOf(
        // this is not going to change
        false to stringResource(R.string.kilometers),
        true to stringResource(R.string.miles)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Modifier
            .fillMaxWidth()
        OutlinedTextField(
            readOnly = true,
            value = units[useImperial] ?: "Kilometers (km)",
            onValueChange = {},
            label = { Text(localizedStringResource(R.string.measurement_units, currentLanguage)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(
                type = MenuAnchorType.PrimaryNotEditable,
                enabled = true)
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