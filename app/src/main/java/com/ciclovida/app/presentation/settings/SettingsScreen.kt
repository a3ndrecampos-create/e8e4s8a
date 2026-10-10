package com.ciclovida.app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Privacidade e Segurança", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Todos os seus dados ficam só neste aparelho. O app não tem permissão de internet.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingRow(
            title = "Biometria",
            description = "Usar impressão digital ou reconhecimento facial para entrar",
            checked = uiState.biometricEnabled,
            onCheckedChange = viewModel::onBiometricToggled
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingRow(
            title = "Modo discreto",
            description = "Disfarça o ícone e o nome do app na tela inicial do celular",
            checked = uiState.discreetModeEnabled,
            onCheckedChange = viewModel::onDiscreetModeToggled
        )
    }
}

@Composable
private fun SettingRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
