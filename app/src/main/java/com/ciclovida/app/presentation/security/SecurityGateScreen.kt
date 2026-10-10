package com.ciclovida.app.presentation.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SecurityGateScreen(
    onAuthenticated: () -> Unit,
    viewModel: SecurityGateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    when (uiState.isPinConfigured) {
        null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        false -> CreatePinContent(errorMessage = uiState.errorMessage, onConfirm = viewModel::onCreatePin)
        true -> VerifyPinContent(
            errorMessage = uiState.errorMessage,
            biometricAvailable = uiState.biometricAvailable,
            onSubmit = viewModel::onSubmitPin,
            onRequestBiometric = viewModel::onRequestBiometric
        )
    }
}

@Composable
private fun CreatePinContent(errorMessage: String?, onConfirm: (String, String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bem-vinda ao CicloVida", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Crie um PIN de 6 dígitos para proteger seus dados. Tudo fica só neste aparelho.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6) pin = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            label = { Text("Novo PIN") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmation,
            onValueChange = { if (it.length <= 6) confirmation = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            label = { Text("Confirme o PIN") }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onConfirm(pin, confirmation) }, enabled = pin.length == 6 && confirmation.length == 6) {
            Text("Criar PIN e continuar")
        }
    }
}

@Composable
private fun VerifyPinContent(
    errorMessage: String?,
    biometricAvailable: Boolean,
    onSubmit: (String) -> Unit,
    onRequestBiometric: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Digite seu PIN", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pinInput,
            onValueChange = { if (it.length <= 6) pinInput = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            label = { Text("PIN de 6 dígitos") }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onSubmit(pinInput) }, enabled = pinInput.length == 6) { Text("Entrar") }

        if (biometricAvailable) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRequestBiometric) { Text("Usar biometria") }
        }
    }
}
