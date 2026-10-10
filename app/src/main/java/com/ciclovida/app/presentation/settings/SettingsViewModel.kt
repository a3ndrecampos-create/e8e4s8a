package com.ciclovida.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciclovida.app.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(val biometricEnabled: Boolean = false, val discreetModeEnabled: Boolean = false)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                biometricEnabled = securityRepository.isBiometricEnabled(),
                discreetModeEnabled = securityRepository.isDiscreetModeEnabled()
            )
        }
    }

    fun onBiometricToggled(enabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setBiometricEnabled(enabled)
            _uiState.update { it.copy(biometricEnabled = enabled) }
        }
    }

    fun onDiscreetModeToggled(enabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setDiscreetModeEnabled(enabled)
            _uiState.update { it.copy(discreetModeEnabled = enabled) }
            // TODO: alternar o activity-alias do launcher (ícone/nome neutros) via PackageManager
            // quando esse recurso for implementado — ver README para o plano completo.
        }
    }
}
