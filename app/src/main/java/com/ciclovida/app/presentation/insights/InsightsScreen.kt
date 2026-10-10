package com.ciclovida.app.presentation.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ciclovida.app.domain.model.CyclePhase
import com.ciclovida.app.domain.model.PhaseWellbeingInsight

/**
 * Visão holística: cruza humor e energia registrados com a fase do ciclo em que
 * foram sentidos, revelando padrões próprios — não genéricos de "internet" — da usuária.
 */
@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (uiState.insights.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                "Continue registrando seu humor e energia por mais alguns ciclos para desbloquear seus padrões pessoais aqui.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Seus padrões por fase do ciclo", style = MaterialTheme.typography.headlineMedium) }
        items(uiState.insights) { insight -> InsightCard(insight) }
    }
}

@Composable
private fun InsightCard(insight: PhaseWellbeingInsight) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(insight.phase.label(), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Energia média: ${"%.1f".format(insight.averageEnergyLevel)} / 5", style = MaterialTheme.typography.bodyLarge)
            if (insight.mostFrequentMoods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Humores mais comuns: ${insight.mostFrequentMoods.joinToString(", ") { it.name.lowercase() }}", style = MaterialTheme.typography.bodyMedium)
            }
            if (insight.mostFrequentSymptoms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Sintomas mais comuns: ${insight.mostFrequentSymptoms.joinToString(", ") { it.name.lowercase().replace("_", " ") }}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Baseado em ${insight.sampleSize} registros", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

private fun CyclePhase.label() = when (this) {
    CyclePhase.MENSTRUAL -> "Fase menstrual"
    CyclePhase.FOLICULAR -> "Fase folicular"
    CyclePhase.OVULATORIA -> "Fase ovulatória"
    CyclePhase.LUTEA -> "Fase lútea"
    CyclePhase.INDEFINIDA -> "Indefinida"
}
