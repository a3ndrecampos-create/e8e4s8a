package com.ciclovida.app.presentation.logentry

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ciclovida.app.domain.model.FlowIntensity
import com.ciclovida.app.domain.model.Mood
import com.ciclovida.app.domain.model.SymptomType
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogEntryScreen(viewModel: LogEntryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedConfirmation) {
        if (uiState.savedConfirmation) {
            snackbarHostState.showSnackbar("Registro salvo")
            viewModel.onConfirmationShown()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    uiState.date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR")))
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item { FlowSection(selected = uiState.flowIntensity, onSelect = viewModel::onFlowSelected) }
            item { SymptomsSection(selected = uiState.selectedSymptoms, onToggle = viewModel::onToggleSymptom) }
            item { MoodSection(selected = uiState.selectedMoods, onToggle = viewModel::onToggleMood) }
            item { EnergySection(level = uiState.energyLevel, onChange = viewModel::onEnergyChanged) }
            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                Button(onClick = viewModel::onSave, modifier = Modifier.fillMaxWidth()) {
                    Text("Salvar registro de hoje")
                }
            }
        }
    }
}

@Composable
private fun FlowSection(selected: FlowIntensity?, onSelect: (FlowIntensity?) -> Unit) {
    Column {
        Text("Fluxo menstrual", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FlowIntensity.entries) { flow ->
                FilterChip(
                    selected = selected == flow,
                    onClick = { onSelect(if (selected == flow) null else flow) },
                    label = { Text(flow.label()) }
                )
            }
        }
    }
}

@Composable
private fun SymptomsSection(selected: Set<SymptomType>, onToggle: (SymptomType) -> Unit) {
    Column {
        Text("Sintomas", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SymptomType.entries.forEach { symptom ->
                FilterChip(
                    selected = symptom in selected,
                    onClick = { onToggle(symptom) },
                    label = { Text(symptom.label()) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MoodSection(selected: Set<Mood>, onToggle: (Mood) -> Unit) {
    Column {
        Text("Humor", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Mood.entries.forEach { mood ->
                FilterChip(
                    selected = mood in selected,
                    onClick = { onToggle(mood) },
                    label = { Text(mood.label()) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EnergySection(level: Int, onChange: (Int) -> Unit) {
    Column {
        Text("Nível de energia", style = MaterialTheme.typography.titleMedium)
        Slider(value = level.toFloat(), onValueChange = { onChange(it.toInt()) }, valueRange = 1f..5f, steps = 3)
    }
}

private fun FlowIntensity.label() = when (this) {
    FlowIntensity.SPOTTING -> "Spotting"
    FlowIntensity.LEVE -> "Leve"
    FlowIntensity.MODERADO -> "Moderado"
    FlowIntensity.INTENSO -> "Intenso"
}

private fun SymptomType.label() = when (this) {
    SymptomType.COLICA -> "Cólica"
    SymptomType.DOR_DE_CABECA -> "Dor de cabeça"
    SymptomType.INCHACO -> "Inchaço"
    SymptomType.SENSIBILIDADE_MAMAS -> "Sensibilidade nas mamas"
    SymptomType.ACNE -> "Acne"
    SymptomType.FADIGA -> "Fadiga"
    SymptomType.NAUSEA -> "Náusea"
    SymptomType.DOR_NAS_COSTAS -> "Dor nas costas"
    SymptomType.ALTERACAO_APETITE -> "Alteração de apetite"
    SymptomType.INSONIA -> "Insônia"
}

private fun Mood.label() = when (this) {
    Mood.FELIZ -> "Feliz"
    Mood.CALMA -> "Calma"
    Mood.IRRITADA -> "Irritada"
    Mood.TRISTE -> "Triste"
    Mood.ANSIOSA -> "Ansiosa"
    Mood.SENSIVEL -> "Sensível"
    Mood.ENERGICA -> "Enérgica"
    Mood.CANSADA -> "Cansada"
}
