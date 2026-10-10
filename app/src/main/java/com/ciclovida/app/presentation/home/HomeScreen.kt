package com.ciclovida.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ciclovida.app.domain.model.CyclePhase
import com.ciclovida.app.domain.model.PredictionConfidence
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(onGoToLogEntry: () -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            uiState.today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                .replaceFirstChar { it.uppercase() } + ", " +
                uiState.today.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("pt", "BR"))),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        PhaseBadge(phase = uiState.currentPhase)

        Spacer(modifier = Modifier.height(32.dp))

        val prediction = uiState.prediction
        if (prediction == null) {
            EmptyStateCard(onGoToLogEntry = onGoToLogEntry)
        } else {
            CountdownCard(daysUntil = uiState.daysUntilNextPeriod ?: 0, confidence = prediction.confidenceLevel)
            Spacer(modifier = Modifier.height(16.dp))
            FertileWindowCard(
                start = prediction.fertileWindowStart.format(DateTimeFormatter.ofPattern("d/MM")),
                end = prediction.fertileWindowEnd.format(DateTimeFormatter.ofPattern("d/MM"))
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onGoToLogEntry, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar hoje")
        }
    }
}

@Composable
private fun PhaseBadge(phase: CyclePhase) {
    val (label, color) = when (phase) {
        CyclePhase.MENSTRUAL -> "Fase menstrual" to MaterialTheme.colorScheme.primary
        CyclePhase.FOLICULAR -> "Fase folicular" to MaterialTheme.colorScheme.tertiary
        CyclePhase.OVULATORIA -> "Fase ovulatória" to MaterialTheme.colorScheme.secondary
        CyclePhase.LUTEA -> "Fase lútea" to MaterialTheme.colorScheme.secondary
        CyclePhase.INDEFINIDA -> "Registre seu ciclo para começar" to MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier.size(160.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = color, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun CountdownCard(daysUntil: Long, confidence: PredictionConfidence) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val text = when {
                daysUntil < 0 -> "Sua menstruação pode estar atrasada"
                daysUntil == 0L -> "Sua menstruação é esperada hoje"
                daysUntil == 1L -> "Sua menstruação é esperada amanhã"
                else -> "Sua menstruação é esperada em $daysUntil dias"
            }
            Text(text, style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Confiança da previsão: ${confidence.label()}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FertileWindowCard(start: String, end: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Janela fértil estimada", style = MaterialTheme.typography.titleMedium)
            Text("$start a $end", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyStateCard(onGoToLogEntry: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Ainda não há dados suficientes para prever seu próximo ciclo.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Registre o início da sua última menstruação para começar.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun PredictionConfidence.label(): String = when (this) {
    PredictionConfidence.INSUFICIENTE -> "insuficiente"
    PredictionConfidence.BAIXA -> "baixa"
    PredictionConfidence.MEDIA -> "média"
    PredictionConfidence.ALTA -> "alta"
}
